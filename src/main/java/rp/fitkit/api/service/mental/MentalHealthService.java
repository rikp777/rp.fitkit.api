package rp.fitkit.api.service.mental;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import rp.fitkit.api.dto.mental.MentalHealthStepDto;
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

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MentalHealthService {

    private final MentalHealthStepRepository stepRepository;
    private final MentalHealthStepTranslationRepository translationRepository;
    private final UserStepProgressRepository userStepProgressRepository;
    private final PerformedActionRepository performedActionRepository;
    private final UserRepository userRepository;

    /**
     * Haalt alle mentale gezondheidsstappen op voor een specifieke gebruiker, inclusief vertalingen en unlock-status.
     *
     * @param userId De ID van de gebruiker.
     * @param languageCode De gewenste taalcode voor de vertalingen.
     * @return Een Flux van DTO's die elke stap vertegenwoordigen.
     */
    public Flux<MentalHealthStepDto> getMentalHealthStepsForUser(String userId, String languageCode) {
        Mono<List<MentalHealthStep>> stepsMono = stepRepository.findAll()
                .sort(Comparator.comparing(MentalHealthStep::getStepNumber))
                .collectList();

        Mono<Map<Long, UserStepProgress>> progressMapMono = userStepProgressRepository.findByUserId(userId)
                .collectMap(UserStepProgress::getMentalHealthStepId);

        return Mono.zip(stepsMono, progressMapMono)
                .flatMapMany(tuple -> {
                    List<MentalHealthStep> steps = tuple.getT1();
                    Map<Long, UserStepProgress> progressMap = tuple.getT2();
                    Map<Integer, MentalHealthStep> stepNumberToStepMap = steps.stream()
                            .collect(Collectors.toMap(MentalHealthStep::getStepNumber, s -> s));

                    return Flux.fromIterable(steps)
                            .concatMap(step -> {
                                MentalHealthStep previousStep = stepNumberToStepMap.get(step.getStepNumber() - 1);
                                boolean isUnlocked = isStepUnlocked(step, previousStep, progressMap);

                                return translationRepository.findByMentalHealthStepIdAndLanguageCode(step.getId(), languageCode)
                                        .switchIfEmpty(translationRepository.findByMentalHealthStepIdAndLanguageCode(step.getId(), "en-US")) // Fallback
                                        .map(translation -> {
                                            int userCompletions = progressMap.getOrDefault(step.getId(), new UserStepProgress()).getCompletionCount();
                                            return toDto(step, translation, isUnlocked, userCompletions);
                                        });
                            });
                });
    }

    /**
     * Verwerkt de voltooiing van een stap door een gebruiker.
     * Deze methode is transactioneel om data-integriteit te garanderen.
     *
     * @param userId De ID van de gebruiker die de actie uitvoert.
     * @param stepId De ID van de voltooide stap.
     * @return Een lege Mono als de operatie slaagt, anders een error.
     */
    @Transactional
    public Mono<Void> performStepAction(String userId, Long stepId) {
        Mono<MentalHealthStep> stepMono = stepRepository.findById(stepId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Mental health step with id " + stepId + " not found.")));

        return userRepository.existsById(userId)
                .flatMap(userExists -> {
                    if (!userExists) {
                        return Mono.error(new ResourceNotFoundException("User with id " + userId + " not found."));
                    }
                    return stepMono;
                })
                .flatMap(step -> isStepUnlockedForUser(userId, step)
                        .flatMap(isUnlocked -> {
                            if (!isUnlocked) {
                                return Mono.error(new AccessDeniedException("This step is not yet unlocked for user " + userId));
                            }

                            PerformedAction action = new PerformedAction();
                            action.setUserId(userId);
                            action.setMentalHealthStepId(stepId);
                            action.setPerformedAt(LocalDateTime.now());

                            Mono<PerformedAction> saveActionMono = performedActionRepository.save(action);

                            Mono<UserStepProgress> updateProgressMono = userStepProgressRepository
                                    .findByUserIdAndMentalHealthStepId(userId, stepId)
                                    .defaultIfEmpty(new UserStepProgress(null, userId, stepId, 0))
                                    .flatMap(progress -> {
                                        progress.setCompletionCount(progress.getCompletionCount() + 1);
                                        return userStepProgressRepository.save(progress);
                                    });

                            return Mono.when(saveActionMono, updateProgressMono);
                        })
                )
                .then();
    }

    private Mono<Boolean> isStepUnlockedForUser(String userId, MentalHealthStep step) {
        if (step.getStepNumber() == 1) {
            return Mono.just(true);
        }
        return stepRepository.findByStepNumber(step.getStepNumber() - 1)
                .flatMap(previousStep -> userStepProgressRepository.findByUserIdAndMentalHealthStepId(userId, previousStep.getId())
                        .map(progress -> progress.getCompletionCount() >= step.getRequiredCompletions())
                        .defaultIfEmpty(false)
                )
                .defaultIfEmpty(false);
    }

    private boolean isStepUnlocked(MentalHealthStep currentStep, MentalHealthStep previousStep, Map<Long, UserStepProgress> progressMap) {
        if (currentStep.getStepNumber() == 1) {
            return true;
        }
        if (previousStep == null) {
            return false; // Should not happen in a consistent dataset
        }
        UserStepProgress previousStepProgress = progressMap.get(previousStep.getId());
        return previousStepProgress != null && previousStepProgress.getCompletionCount() >= currentStep.getRequiredCompletions();
    }

    private MentalHealthStepDto toDto(MentalHealthStep step, MentalHealthStepTranslation translation, boolean isUnlocked, int userCompletions) {
        return new MentalHealthStepDto(
                step.getId(),
                step.getStepNumber(),
                translation.getTitle(),
                translation.getDescription(),
                translation.getPurpose(),
                isUnlocked,
                step.getRequiredCompletions(),
                userCompletions
        );
    }
}

