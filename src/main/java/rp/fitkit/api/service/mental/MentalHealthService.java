package rp.fitkit.api.service.mental;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
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
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
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
    public Flux<MentalHealthStepDto> getMentalHealthStepsForUser(UUID userId, String languageCode) {
        log.info("Fetching all mental health steps for user: {} with language: {}", userId, languageCode);
        return getStepsAndProgress(userId)
                .flatMapMany(tuple -> {
                    List<MentalHealthStep> steps = tuple.getT1();
                    Map<Long, UserStepProgress> progressMap = tuple.getT2();
                    Map<Integer, MentalHealthStep> stepNumberToStepMap = steps.stream()
                            .collect(Collectors.toMap(MentalHealthStep::getStepNumber, s -> s));

                    return Flux.fromIterable(steps)
                            .concatMap(step -> {
                                MentalHealthStep previousStep = stepNumberToStepMap.get(step.getStepNumber() - 1);
                                boolean isUnlocked = isStepUnlocked(step, previousStep, progressMap);
                                log.trace("Step {} for user {} is unlocked: {}", step.getStepNumber(), userId, isUnlocked);

                                return translationRepository.findByMentalHealthStepIdAndLanguageCode(step.getId(), languageCode)
                                        .switchIfEmpty(Mono.defer(() -> {
                                            log.warn("No translation found for stepId {} and language {}. Falling back to en-US.", step.getId(), languageCode);
                                            return translationRepository.findByMentalHealthStepIdAndLanguageCode(step.getId(), "en-US");
                                        }))

                                        .map(translation -> {
                                            int userCompletions = progressMap.getOrDefault(step.getId(), new UserStepProgress()).getCompletionCount();
                                            return toDto(step, translation, isUnlocked, userCompletions);
                                        });
                            });
                });
    }

    /**
     * Retrieves a specific mental health step for a user, including progress and translation.
     *
     * @param userId       The ID of the user.
     * @param stepId       The ID of the step to retrieve.
     * @param languageCode The desired language code for translations.
     * @return A Mono containing the step DTO, or an error if not found or not accessible.
     */
    public Mono<MentalHealthStepDto> getMentalHealthStepForUser(UUID userId, Long stepId, String languageCode) {
        log.info("Fetching mental health stepId: {} for user: {} with language: {}", stepId, userId, languageCode);

        Mono<MentalHealthStep> stepMono = stepRepository.findById(stepId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Mental health step with id " + stepId + " not found.")));

        return stepMono.flatMap(step -> {
            Mono<Boolean> isUnlockedMono = isStepUnlockedForUser(userId, step);

            Mono<MentalHealthStepTranslation> translationMono = translationRepository.findByMentalHealthStepIdAndLanguageCode(stepId, languageCode)
                    .switchIfEmpty(Mono.defer(() -> {
                        log.warn("No translation for stepId {} and language {}. Falling back to en-US.", stepId, languageCode);
                        return translationRepository.findByMentalHealthStepIdAndLanguageCode(stepId, "en-US");
                    }));

            Mono<Integer> userCompletionsMono = userStepProgressRepository.findByUserIdAndMentalHealthStepId(userId, stepId)
                    .map(UserStepProgress::getCompletionCount)
                    .defaultIfEmpty(0);

            return Mono.zip(isUnlockedMono, translationMono, userCompletionsMono)
                    .doOnSuccess(tuple -> log.debug("Assembled DTO for stepId: {}. Unlocked: {}, Completions: {}", stepId, tuple.getT1(), tuple.getT3()))
                    .map(tuple -> toDto(step, tuple.getT2(), tuple.getT1(), tuple.getT3()));
        });
    }


    /**
     * Retrieves the suggested mental health step for a user.
     * The suggested step is the first one that is unlocked but not yet completed.
     *
     * @param userId The ID of the user.
     * @param languageCode The desired language code for translations.
     * @return A Mono containing the suggested step DTO, or empty if all steps are completed.
     */
    public Mono<MentalHealthStepDto> getSuggestedStepForUser(UUID userId, String languageCode) {
        log.info("Fetching suggested mental health step for user: {} with language: {}", userId, languageCode);

        return getStepsAndProgress(userId)
                .flatMap(tuple -> {
                    List<MentalHealthStep> steps = tuple.getT1();
                    Map<Long, UserStepProgress> progressMap = tuple.getT2();
                    Map<Integer, MentalHealthStep> stepNumberToStepMap = steps.stream()
                            .collect(Collectors.toMap(MentalHealthStep::getStepNumber, s -> s));

                    return Flux.fromIterable(steps)
                            .filter(step -> {
                                MentalHealthStep previousStep = stepNumberToStepMap.get(step.getStepNumber() - 1);
                                boolean isUnlocked = isStepUnlocked(step, previousStep, progressMap);
                                int completions = progressMap.getOrDefault(step.getId(), new UserStepProgress()).getCompletionCount();
                                return isUnlocked && completions < step.getRequiredCompletions();
                            })
                            .next() // Take the first step that matches the criteria
                            .flatMap(step -> {
                                log.info("Found suggested step for user {}: stepId {}", userId, step.getId());
                                int userCompletions = progressMap.getOrDefault(step.getId(), new UserStepProgress()).getCompletionCount();
                                return translationRepository.findByMentalHealthStepIdAndLanguageCode(step.getId(), languageCode)
                                        .switchIfEmpty(translationRepository.findByMentalHealthStepIdAndLanguageCode(step.getId(), "en-US")) // Fallback
                                        .map(translation -> toDto(step, translation, true, userCompletions));
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
    public Mono<Void> performStepAction(UUID userId, Long stepId) {
        log.info("User {} is performing action for stepId: {}", userId, stepId);

        Mono<MentalHealthStep> stepMono = stepRepository.findById(stepId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Mental health step with id " + stepId + " not found.")));

        return stepMono
                .flatMap(step -> isStepUnlockedForUser(userId, step)
                        .flatMap(isUnlocked -> {
                            if (!isUnlocked) {
                                log.warn("User {} attempted to perform action on locked stepId: {}", userId, stepId);
                                return Mono.error(new AccessDeniedException("This step is not yet unlocked for user " + userId));
                            }
                            log.debug("Step {} is unlocked for user {}. Proceeding with action.", stepId, userId);

                            PerformedAction action = new PerformedAction(null, userId, stepId, LocalDateTime.now());

                            Mono<PerformedAction> saveActionMono = performedActionRepository.save(action)
                                    .doOnSuccess(sa -> log.debug("Saved new performed action with ID: {}", sa.getId()));

                            Mono<UserStepProgress> updateProgressMono = userStepProgressRepository
                                    .findByUserIdAndMentalHealthStepId(userId, stepId)
                                    .defaultIfEmpty(new UserStepProgress(null, userId, stepId, 0))
                                    .doOnSuccess(p -> log.debug("Current completion count for user {} on step {} is {}. Incrementing.", userId, stepId, p.getCompletionCount()))
                                    .flatMap(progress -> {
                                        progress.setCompletionCount(progress.getCompletionCount() + 1);
                                        return userStepProgressRepository.save(progress);
                                    })
                                    .doOnSuccess(up -> log.info("Updated completion count for user {} on step {} to {}", userId, stepId, up.getCompletionCount()));

                            return Mono.when(saveActionMono, updateProgressMono);
                        })
                )
                .then();
    }

    /**
     * Asynchronously fetches all mental health steps and the progress for a specific user.
     * <p>
     * This method concurrently retrieves two sets of data:
     * <ol>
     *     <li>All {@link MentalHealthStep} entities from the repository, sorted by {@code stepNumber}.</li>
     *     <li>All {@link UserStepProgress} records for the specified {@code userId}, organized into a Map where the key is the {@code mentalHealthStepId}.</li>
     * </ol>
     * It then combines these two results into a single {@link Mono} that emits a {@link Tuple2}.
     * This approach is efficient as it fetches the two independent data streams in parallel.
     *
     * @param userId The ID of the user for whom to fetch the progress.
     * @return A {@code Mono<Tuple2<List<MentalHealthStep>, Map<Long, UserStepProgress>>>} which, upon completion,
     *         emits a tuple containing the sorted list of all steps (T1) and a map of the user's progress for those steps (T2).
     */
    private Mono<Tuple2<List<MentalHealthStep>, Map<Long, UserStepProgress>>> getStepsAndProgress(UUID userId) {
        final String logPrefix = String.format("[HEALTH_HELPER] userId=%s:", userId);
        log.debug("{} Fetching all steps and progress.", logPrefix);
        Mono<List<MentalHealthStep>> stepsMono = stepRepository.findAll()
                .sort(Comparator.comparing(MentalHealthStep::getStepNumber))
                .collectList();

        Mono<Map<Long, UserStepProgress>> progressMapMono = userStepProgressRepository.findByUserId(userId)
                .collectMap(UserStepProgress::getMentalHealthStepId);

        return Mono.zip(stepsMono, progressMapMono)
                .doOnSuccess(tuple -> log.debug("{} Fetched {} total steps and {} progress entries.", logPrefix, tuple.getT1().size(), tuple.getT2().size()));
    }

    private Mono<Boolean> isStepUnlockedForUser(UUID userId, MentalHealthStep step) {
        if (step.getStepNumber() == 1) {
            return Mono.just(true);
        }
        return stepRepository.findByStepNumber(step.getStepNumber() - 1)
                .flatMap(previousStep -> userStepProgressRepository.findByUserIdAndMentalHealthStepId(userId, previousStep.getId())
                        .map(progress -> progress.getCompletionCount() >= previousStep.getRequiredCompletions())
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
        return previousStepProgress != null && previousStepProgress.getCompletionCount() >= previousStep.getRequiredCompletions();
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

