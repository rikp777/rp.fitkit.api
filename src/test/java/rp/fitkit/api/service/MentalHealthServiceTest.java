package rp.fitkit.api.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import rp.fitkit.api.exception.ResourceNotFoundException;
import rp.fitkit.api.model.mental.MentalHealthStep;
import rp.fitkit.api.model.mental.PerformedAction;
import rp.fitkit.api.model.mental.UserStepProgress;
import rp.fitkit.api.model.mental.translation.MentalHealthStepTranslation;
import rp.fitkit.api.repository.user.UserRepository;
import rp.fitkit.api.repository.mental.MentalHealthStepRepository;
import rp.fitkit.api.repository.mental.MentalHealthStepTranslationRepository;
import rp.fitkit.api.repository.mental.PerformedActionRepository;
import rp.fitkit.api.repository.mental.UserStepProgressRepository;
import rp.fitkit.api.service.mental.MentalHealthService;

import java.time.Duration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MentalHealthServiceTest {

    @Mock
    private MentalHealthStepRepository stepRepository;
    @Mock
    private MentalHealthStepTranslationRepository translationRepository;
    @Mock
    private UserStepProgressRepository userStepProgressRepository;
    @Mock
    private PerformedActionRepository performedActionRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MentalHealthService mentalHealthService;

    private MentalHealthStep step1, step2;
    private MentalHealthStepTranslation translation1, translation2;
    private UserStepProgress progressForStep1;

    @BeforeEach
    void setUp() {
        step1 = new MentalHealthStep();
        step1.setId(1L);
        step1.setStepNumber(1);
        step1.setRequiredCompletions(5);

        step2 = new MentalHealthStep();
        step2.setId(2L);
        step2.setStepNumber(2);
        step2.setRequiredCompletions(5);

        translation1 = new MentalHealthStepTranslation(1L, "en-US", "Title 1", "Desc 1", "Purp 1");
        translation2 = new MentalHealthStepTranslation(2L, "en-US", "Title 2", "Desc 2", "Purp 2");

        progressForStep1 = new UserStepProgress(100L, "user-123", 1L, 5);

    }

    @Test
    @DisplayName("getMentalHealthStepsForUser should return unlocked step 1 and locked step 2")
    void getMentalHealthStepsForUser_WithPartialProgress() {
        // Arrange
        when(stepRepository.findAll()).thenReturn(Flux.just(step1, step2));
        when(userStepProgressRepository.findByUserId("user-123")).thenReturn(Flux.just(new UserStepProgress(100L, "user-123", 1L, 4)));
        when(translationRepository.findByMentalHealthStepIdAndLanguageCode(1L, "en-US")).thenReturn(Mono.just(translation1));
        when(translationRepository.findByMentalHealthStepIdAndLanguageCode(2L, "en-US")).thenReturn(Mono.just(translation2));

        // Act
        var result = mentalHealthService.getMentalHealthStepsForUser("user-123", "en-US");

        // Assert
        StepVerifier.create(result)
                .assertNext(dto -> {
                    assert dto.getId() == 1L;
                    assert dto.isUnlocked();
                    assert dto.getUserCompletions() == 4;
                })
                .assertNext(dto -> {
                    assert dto.getId() == 2L;
                    assert !dto.isUnlocked();
                    assert dto.getUserCompletions() == 0;
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("getMentalHealthStepsForUser should unlock step 2 when requirements are met")
    void getMentalHealthStepsForUser_WhenRequirementsMet() {
        // Arrange
        when(stepRepository.findAll()).thenReturn(Flux.just(step1, step2));
        when(userStepProgressRepository.findByUserId("user-123")).thenReturn(Flux.just(progressForStep1));
        when(translationRepository.findByMentalHealthStepIdAndLanguageCode(1L, "en-US")).thenReturn(Mono.just(translation1));
        when(translationRepository.findByMentalHealthStepIdAndLanguageCode(2L, "en-US")).thenReturn(Mono.just(translation2));

        // Act
        var result = mentalHealthService.getMentalHealthStepsForUser("user-123", "en-US");

        // Assert
        StepVerifier.create(result)
                .assertNext(dto -> {
                    assert dto.getId() == 1L;
                    assert dto.isUnlocked();
                })
                .assertNext(dto -> {
                    assert dto.getId() == 2L;
                    assert dto.isUnlocked();
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("performStepAction should succeed for an unlocked step")
    void performStepAction_Success() {
        // Arrange
        when(stepRepository.findById(1L)).thenReturn(Mono.just(step1));
        when(performedActionRepository.save(any(PerformedAction.class))).thenReturn(Mono.just(new PerformedAction()));
        when(userStepProgressRepository.findByUserIdAndMentalHealthStepId("user-123", 1L)).thenReturn(Mono.empty());
        when(userStepProgressRepository.save(any(UserStepProgress.class))).thenReturn(Mono.just(new UserStepProgress()));

        // Act
        var result = mentalHealthService.performStepAction("user-123", 1L);

        // Assert
        StepVerifier.create(result).verifyComplete();
    }


    @Test
    @DisplayName("performStepAction should fail for a locked step")
    void performStepAction_LockedStep() {
        // Arrange
        when(stepRepository.findById(2L)).thenReturn(Mono.just(step2));
        when(stepRepository.findByStepNumber(1)).thenReturn(Mono.just(step1));
        when(userStepProgressRepository.findByUserIdAndMentalHealthStepId("user-123", 1L)).thenReturn(Mono.empty()); // No progress on step 1

        // Act
        var result = mentalHealthService.performStepAction("user-123", 2L);

        // Assert
        StepVerifier.create(result)
                .expectError(AccessDeniedException.class)
                .verify();
    }

    @Test
    @DisplayName("performStepAction should fail for a non-existent step")
    void performStepAction_StepNotFound() {
        // Arrange
        when(stepRepository.findById(99L)).thenReturn(Mono.empty());

        // Act
        var result = mentalHealthService.performStepAction("user-123", 99L);

        // Assert
        StepVerifier.create(result)
                .expectError(ResourceNotFoundException.class)
                .verify();
    }

    @Test
    @DisplayName("getSuggestedStepForUser should suggest step 2 when step 1 is completed")
    void getSuggestedStepForUser_shouldSuggestStep2() {
        // Arrange
        var progressStep1 = new UserStepProgress(100L, "user-123", 1L, 5); // Step 1 is complete
        var progressStep2 = new UserStepProgress(101L, "user-123", 2L, 2); // Step 2 is incomplete

        when(stepRepository.findAll()).thenReturn(Flux.just(step1, step2));
        when(userStepProgressRepository.findByUserId("user-123")).thenReturn(Flux.just(progressStep1, progressStep2));
        when(translationRepository.findByMentalHealthStepIdAndLanguageCode(2L, "en-US")).thenReturn(Mono.just(translation2));

        // Act
        var result = mentalHealthService.getSuggestedStepForUser("user-123", "en-US");

        // Assert
        StepVerifier.create(result)
                .assertNext(dto -> {
                    assert dto.getId() == 2L;
                    assert dto.getStepNumber() == 2;
                    assert dto.getUserCompletions() == 2;
                    assert dto.getRequiredCompletions() == 5;
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("getSuggestedStepForUser should return empty when all steps are completed")
    void getSuggestedStepForUser_shouldReturnEmpty() {
        // Arrange
        var progressStep1 = new UserStepProgress(100L, "user-123", 1L, 5);
        var progressStep2 = new UserStepProgress(101L, "user-123", 2L, 5);

        when(stepRepository.findAll()).thenReturn(Flux.just(step1, step2));
        when(userStepProgressRepository.findByUserId("user-123")).thenReturn(Flux.just(progressStep1, progressStep2));

        // Act
        var result = mentalHealthService.getSuggestedStepForUser("user-123", "en-US");

        // Assert
        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    @DisplayName("getMentalHealthStepById should return correct DTO for an unlocked step")
    void getMentalHealthStepById_unlocked() {
        // Arrange
        when(stepRepository.findById(1L)).thenReturn(Mono.just(step1));
        when(translationRepository.findByMentalHealthStepIdAndLanguageCode(1L, "en-US")).thenReturn(Mono.just(translation1));
        when(userStepProgressRepository.findByUserIdAndMentalHealthStepId("user-123", 1L)).thenReturn(Mono.just(new UserStepProgress(100L, "user-123", 1L, 3)));

        // Act
        var result = mentalHealthService.getMentalHealthStepForUser("user-123", 1L, "en-US");

        // Assert
        StepVerifier.create(result)
                .assertNext(dto -> {
                    assert dto.getId() == 1L;
                    assert dto.getTitle().equals("Title 1");
                    assert dto.isUnlocked();
                    assert dto.getUserCompletions() == 3;
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("getMentalHealthStepById should return correct DTO for a locked step")
    void getMentalHealthStepById_locked() {
        // Arrange
        when(stepRepository.findById(2L)).thenReturn(Mono.just(step2));
        when(stepRepository.findByStepNumber(1)).thenReturn(Mono.just(step1));
        when(userStepProgressRepository.findByUserIdAndMentalHealthStepId("user-123", 1L)).thenReturn(Mono.just(new UserStepProgress(100L, "user-123", 1L, 3))); // Step 1 NOT complete
        when(translationRepository.findByMentalHealthStepIdAndLanguageCode(2L, "en-US")).thenReturn(Mono.just(translation2));
        when(userStepProgressRepository.findByUserIdAndMentalHealthStepId("user-123", 2L)).thenReturn(Mono.empty());

        // Act
        var result = mentalHealthService.getMentalHealthStepForUser("user-123", 2L, "en-US");

        // Assert
        StepVerifier.create(result)
                .assertNext(dto -> {
                    assert dto.getId() == 2L;
                    assert !dto.isUnlocked();
                    assert dto.getUserCompletions() == 0;
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("getMentalHealthStepForUser should return ResourceNotFoundException for non-existent step")
    void getMentalHealthStepForUser_notFound() {
        // Arrange
        when(stepRepository.findById(99L)).thenReturn(Mono.empty());

        // Act
        var result = mentalHealthService.getMentalHealthStepForUser("user-123", 99L, "en-US");

        // Assert
        StepVerifier.create(result)
                .expectError(ResourceNotFoundException.class)
                .verify();
    }

    @Test
    @DisplayName("getMentalHealthStepForUser should complete within 50 milliseconds")
    void getMentalHealthStepForUser_performanceCheck() {
        // Arrange
        when(stepRepository.findById(1L)).thenReturn(Mono.just(step1));
        when(translationRepository.findByMentalHealthStepIdAndLanguageCode(1L, "en-US")).thenReturn(Mono.just(translation1));
        when(userStepProgressRepository.findByUserIdAndMentalHealthStepId("user-123", 1L)).thenReturn(Mono.just(new UserStepProgress(100L, "user-123", 1L, 3)));

        // Act
        var result = mentalHealthService.getMentalHealthStepForUser("user-123", 1L, "en-US");

        // Assert
        StepVerifier.create(result)
                .expectNextCount(1)
                .expectComplete()
                .verify(Duration.ofMillis(50));
    }

    // --- Performance Tests ---
    @Test
    @DisplayName("getMentalHealthStepsForUser should complete within 50 milliseconds")
    void getMentalHealthStepsForUser_performanceCheck() {
        // Arrange
        when(stepRepository.findAll()).thenReturn(Flux.just(step1, step2));
        when(userStepProgressRepository.findByUserId("user-123")).thenReturn(Flux.just(progressForStep1));
        when(translationRepository.findByMentalHealthStepIdAndLanguageCode(1L, "en-US")).thenReturn(Mono.just(translation1));
        when(translationRepository.findByMentalHealthStepIdAndLanguageCode(2L, "en-US")).thenReturn(Mono.just(translation2));

        // Act
        var result = mentalHealthService.getMentalHealthStepsForUser("user-123", "en-US");

        // Assert
        StepVerifier.create(result)
                .expectNextCount(2)
                .expectComplete()
                .verify(Duration.ofMillis(50));
    }

    @Test
    @DisplayName("getSuggestedStepForUser should complete within 50 milliseconds")
    void getSuggestedStepForUser_performanceCheck() {
        // Arrange
        var progressStep1 = new UserStepProgress(100L, "user-123", 1L, 5); // Step 1 is complete
        var progressStep2 = new UserStepProgress(101L, "user-123", 2L, 2); // Step 2 is incomplete

        when(stepRepository.findAll()).thenReturn(Flux.just(step1, step2));
        when(userStepProgressRepository.findByUserId("user-123")).thenReturn(Flux.just(progressStep1, progressStep2));
        when(translationRepository.findByMentalHealthStepIdAndLanguageCode(2L, "en-US")).thenReturn(Mono.just(translation2));

        // Act
        var result = mentalHealthService.getSuggestedStepForUser("user-123", "en-US");

        // Assert
        StepVerifier.create(result)
                .expectNextCount(1)
                .expectComplete()
                .verify(Duration.ofMillis(50));
    }

    @Test
    @DisplayName("performStepAction should complete within 50 milliseconds")
    void performStepAction_performanceCheck() {
        // Arrange
        when(stepRepository.findById(1L)).thenReturn(Mono.just(step1));
        when(performedActionRepository.save(any(PerformedAction.class))).thenReturn(Mono.just(new PerformedAction()));
        when(userStepProgressRepository.findByUserIdAndMentalHealthStepId("user-123", 1L)).thenReturn(Mono.empty());
        when(userStepProgressRepository.save(any(UserStepProgress.class))).thenReturn(Mono.just(new UserStepProgress()));

        // Act
        var result = mentalHealthService.performStepAction("user-123", 1L);

        // Assert
        StepVerifier.create(result)
                .expectComplete()
                .verify(Duration.ofMillis(50));
    }
}

