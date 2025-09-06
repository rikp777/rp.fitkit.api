package rp.fitkit.api.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import rp.fitkit.api.dto.mental.request.CreateMentalHealthStepRequest;
import rp.fitkit.api.dto.mental.request.MentalHealthStepTranslationRequest;
import rp.fitkit.api.exception.ResourceNotFoundException;
import rp.fitkit.api.model.mental.MentalHealthStep;
import rp.fitkit.api.model.mental.translation.MentalHealthStepTranslation;
import rp.fitkit.api.repository.mental.MentalHealthStepRepository;
import rp.fitkit.api.repository.mental.MentalHealthStepTranslationRepository;
import rp.fitkit.api.service.mental.AdminMentalHealthService;

import java.time.Duration;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminMentalHealthServiceTest {

    @Mock
    private MentalHealthStepRepository stepRepository;

    @Mock
    private MentalHealthStepTranslationRepository translationRepository;

    @InjectMocks
    private AdminMentalHealthService adminMentalHealthService;

    private MentalHealthStep step1;
    private MentalHealthStep step2;
    private CreateMentalHealthStepRequest createRequest;

    @BeforeEach
    void setUp() {
        step1 = new MentalHealthStep();
        step1.setId(1L);
        step1.setStepNumber(1);
        step1.setRequiredCompletions(5);

        step2 = new MentalHealthStep();
        step2.setId(2L);
        step2.setStepNumber(2);
        step2.setRequiredCompletions(10);

        MentalHealthStepTranslationRequest translationRequest = new MentalHealthStepTranslationRequest(
                "en-US", "Title", "Description", "Purpose"
        );
        createRequest = new CreateMentalHealthStepRequest(
                3, 15, List.of(translationRequest)
        );
    }

    @Test
    @DisplayName("createMentalHealthStep should save step and its translations")
    void createMentalHealthStep_shouldSucceed() {
        // Arrange
        when(stepRepository.save(any(MentalHealthStep.class))).thenReturn(Mono.just(step1));
        when(translationRepository.save(any(MentalHealthStepTranslation.class)))
                .thenReturn(Mono.just(new MentalHealthStepTranslation()));

        // Act
        Mono<MentalHealthStep> result = adminMentalHealthService.createMentalHealthStep(createRequest);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(savedStep -> savedStep.getId().equals(1L))
                .verifyComplete();

        verify(stepRepository, times(1)).save(any(MentalHealthStep.class));
        verify(translationRepository, times(1)).save(any(MentalHealthStepTranslation.class));
    }

    @Test
    @DisplayName("getAllMentalHealthSteps should return all steps from repository")
    void getAllMentalHealthSteps_shouldSucceed() {
        // Arrange
        when(stepRepository.findAll()).thenReturn(Flux.just(step1, step2));

        // Act
        Flux<MentalHealthStep> result = adminMentalHealthService.getAllMentalHealthSteps();

        // Assert
        StepVerifier.create(result)
                .expectNext(step1)
                .expectNext(step2)
                .verifyComplete();

        verify(stepRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("deleteMentalHealthStep should complete when step exists")
    void deleteMentalHealthStep_shouldSucceed() {
        // Arrange
        when(stepRepository.findById(1L)).thenReturn(Mono.just(step1));
        when(stepRepository.delete(step1)).thenReturn(Mono.empty());

        // Act
        Mono<Void> result = adminMentalHealthService.deleteMentalHealthStep(1L);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();

        verify(stepRepository, times(1)).findById(1L);
        verify(stepRepository, times(1)).delete(step1);
    }

    @Test
    @DisplayName("deleteMentalHealthStep should fail when step does not exist")
    void deleteMentalHealthStep_shouldFail_whenNotFound() {
        // Arrange
        when(stepRepository.findById(99L)).thenReturn(Mono.empty());

        // Act
        Mono<Void> result = adminMentalHealthService.deleteMentalHealthStep(99L);

        // Assert
        StepVerifier.create(result)
                .expectError(ResourceNotFoundException.class)
                .verify();

        verify(stepRepository, times(1)).findById(99L);
        verify(stepRepository, never()).delete(any());
    }

    // --- Performance Tests ---
    @Test
    @DisplayName("createMentalHealthStep should complete within 200 milliseconds")
    void createMentalHealthStep_performanceCheck() {
        // Arrange
        when(stepRepository.save(any(MentalHealthStep.class))).thenReturn(Mono.just(step1));
        when(translationRepository.save(any(MentalHealthStepTranslation.class)))
                .thenReturn(Mono.just(new MentalHealthStepTranslation()));

        // Act
        Mono<MentalHealthStep> result = adminMentalHealthService.createMentalHealthStep(createRequest);

        // Assert
        StepVerifier.create(result)
                .expectNextCount(1)
                .expectComplete()
                .verify(Duration.ofMillis(200));
    }

    @Test
    @DisplayName("getAllMentalHealthSteps should complete within 150 milliseconds")
    void getAllMentalHealthSteps_performanceCheck() {
        // Arrange
        when(stepRepository.findAll()).thenReturn(Flux.just(step1, step2));

        // Act
        Flux<MentalHealthStep> result = adminMentalHealthService.getAllMentalHealthSteps();

        // Assert
        StepVerifier.create(result)
                .expectNextCount(2)
                .expectComplete()
                .verify(Duration.ofMillis(150));
    }

    @Test
    @DisplayName("deleteMentalHealthStep should complete within 150 milliseconds")
    void deleteMentalHealthStep_performanceCheck() {
        // Arrange
        when(stepRepository.findById(1L)).thenReturn(Mono.just(step1));
        when(stepRepository.delete(step1)).thenReturn(Mono.empty());

        // Act
        Mono<Void> result = adminMentalHealthService.deleteMentalHealthStep(1L);

        // Assert
        StepVerifier.create(result)
                .expectComplete()
                .verify(Duration.ofMillis(150));
    }
}

