package rp.fitkit.api.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import rp.fitkit.api.dto.ExerciseLogDto;
import rp.fitkit.api.exception.ResourceNotFoundException;
import rp.fitkit.api.model.ExerciseSession;
import rp.fitkit.api.dto.ExerciseSessionResponseDto;
import rp.fitkit.api.model.SetLog;
import rp.fitkit.api.model.WorkoutSuggestion;
import reactor.core.publisher.Mono;
import rp.fitkit.api.repository.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor
public class WorkoutSuggestionService {

    // --- Constanten ---
    private static final int DEFAULT_MIN_REPS = 8;
    private static final int DEFAULT_MAX_REPS = 12;
    private static final int DEFAULT_TARGET_SETS = 3;
    private static final double WEIGHT_INCREMENT = 2.5;

    // --- Repositories ---
    private final ExerciseSessionRepository exerciseSessionRepository;
    private final ExerciseTemplateRepository exerciseTemplateRepository;
    private final SetLogRepository setLogRepository;
    private final WorkoutTemplateRepository workoutTemplateRepository;
    private final WorkoutPlanRepository planRepository;

    /**
     * Publieke methode voor suggesties gebaseerd op een plan.
     */
    public Mono<WorkoutSuggestion> getSuggestionForPlannedExercise(String userId, String exerciseTemplateId) {
        log.debug("Requesting suggestion for user '{}' and exerciseTemplateId '{}'", userId, exerciseTemplateId);

        return exerciseTemplateRepository.findById(exerciseTemplateId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Geplande oefening niet gevonden met ID: " + exerciseTemplateId)))
                .flatMap(exerciseTemplate -> {
                    return workoutTemplateRepository.findById(exerciseTemplate.getWorkoutTemplateId())
                            .flatMap(workoutTemplate -> planRepository.findById(workoutTemplate.getWorkoutPlanId()))
                            .flatMap(workoutPlan -> {
                                if (!workoutPlan.getUserId().equals(userId)) {
                                    return Mono.error(new AccessDeniedException("Gebruiker heeft geen toegang tot dit workoutplan."));
                                }
                                log.debug("Eigendom geverifieerd. Gebruiker '{}' mag suggestie voor plan '{}' opvragen.", userId, workoutPlan.getName());
                                return Mono.just(exerciseTemplate);
                            });
                })
                .flatMap(validatedExerciseTemplate -> {
                    log.debug("2: Laatste sessie ophalen voor oefening '{}'", validatedExerciseTemplate.getExerciseName());
                    String exerciseName = validatedExerciseTemplate.getExerciseName();

                    return getLastSessionWithSets(userId, exerciseName)
                            .map(lastSession -> {
                                log.debug("3a: Laatste sessie gevonden. Algoritme wordt toegepast.");
                                int minReps = validatedExerciseTemplate.getTargetRepsMin() != null ? validatedExerciseTemplate.getTargetRepsMin() : DEFAULT_MIN_REPS;
                                int maxReps = validatedExerciseTemplate.getTargetRepsMax() != null ? validatedExerciseTemplate.getTargetRepsMax() : DEFAULT_MAX_REPS;
                                int targetSets = validatedExerciseTemplate.getTargetSets() != null ? validatedExerciseTemplate.getTargetSets() : DEFAULT_TARGET_SETS;
                                return applyDoubleProgressionLogic(exerciseName, lastSession, minReps, maxReps, targetSets);
                            })
                            .defaultIfEmpty(createDefaultSuggestion(exerciseName))
                            .doOnSuccess(suggestion -> {
                                log.debug("3b: Geen laatste sessie gevonden of suggestie is de default.");
                            });
                })
                .doOnError(error -> {
                    if (!(error instanceof ResourceNotFoundException || error instanceof AccessDeniedException)) {
                        log.error("Er is een onverwachte fout opgetreden in de suggestie-keten", error);
                    }
                });
    }


    /**
     * Publieke methode voor ad-hoc/freestyle suggesties.
     */
    public Mono<WorkoutSuggestion> getSuggestionForAdHocExercise(String userId, String exerciseName) {
        return getLastSessionWithSets(userId, exerciseName)
                .map(lastSession ->
                        applyDoubleProgressionLogic(exerciseName, lastSession, DEFAULT_MIN_REPS, DEFAULT_MAX_REPS, DEFAULT_TARGET_SETS)
                )
                .defaultIfEmpty(createDefaultSuggestion(exerciseName));
    }

    /**
     * Hulp-methode om de laatste sessie inclusief sets op te halen.
     */
    private Mono<ExerciseSession> getLastSessionWithSets(String userId, String exerciseName) {
        return exerciseSessionRepository.findByUserIdAndExerciseNameOrderByDateDesc(userId, exerciseName)
                .next()
                .flatMap(session ->
                        setLogRepository.findByExerciseSessionId(session.getId())
                                .collectList()
                                .map(sets -> {
                                    session.setSets(sets);
                                    return session;
                                })
                );
    }

    /**
     * De centrale, private hulp-methode met de kernlogica van Double Progression met RPE.
     */
    private WorkoutSuggestion applyDoubleProgressionLogic(String exerciseName, ExerciseSession lastSession, int minReps, int maxReps, int targetSets) {
        log.debug("Applying Double Progression for '{}' with rep range {}-{}", exerciseName, minReps, maxReps);

        if (lastSession.getSets().isEmpty()) return createDefaultSuggestion(exerciseName);

        double lastWeight = lastSession.getSets().get(0).getWeight();
        double averageRpe = lastSession.getSets().stream().mapToInt(SetLog::getRpe).average().orElse(0.0);
        long setsCompletedAtMaxReps = lastSession.getSets().stream().filter(set -> set.getReps() >= maxReps).count();
        List<SetLog> suggestedSets = new ArrayList<>();
        String message;

        if (setsCompletedAtMaxReps >= targetSets) {
            if (averageRpe >= 9.0) {
                message = String.format("Top! Rep-doel (%d) gehaald. Het was zwaar (avg RPE %.1f), dus consolideer op %s kg.", maxReps, averageRpe, lastWeight);
                for (int i = 0; i < targetSets; i++) suggestedSets.add(new SetLog(null, minReps, lastWeight, 0));
            } else {
                double newWeight = lastWeight + WEIGHT_INCREMENT;
                message = String.format("Uitstekend! Rep-doel gehaald. Tijd voor meer gewicht: %s kg voor %d reps.", newWeight, minReps);
                for (int i = 0; i < targetSets; i++) suggestedSets.add(new SetLog(null, minReps, newWeight, 0));
            }
        } else {
            message = String.format("Probeer deze keer meer herhalingen te doen met %s kg. Mik op de %d herhalingen!", lastWeight, maxReps);
            for (SetLog lastSet : lastSession.getSets()) {
                int suggestedReps = Math.min(lastSet.getReps() + 1, maxReps);
                suggestedSets.add(new SetLog(null, suggestedReps, lastSet.getWeight(), 0));
            }
        }
        return new WorkoutSuggestion(exerciseName, suggestedSets, message);
    }

    /**
     * Hulp-methode om een default beginnerssuggestie te maken.
     */
    private WorkoutSuggestion createDefaultSuggestion(String exerciseName) {
        List<SetLog> suggestedSets = new ArrayList<>();
        suggestedSets.add(new SetLog(null, 10, 20.0, 0));
        suggestedSets.add(new SetLog(null, 10, 20.0, 0));
        suggestedSets.add(new SetLog(null, 10, 20.0, 0));
        String message = "Start met deze basis voor " + exerciseName + ". Log je training om betere suggesties te krijgen!";
        return new WorkoutSuggestion(exerciseName, suggestedSets, message);
    }
}
