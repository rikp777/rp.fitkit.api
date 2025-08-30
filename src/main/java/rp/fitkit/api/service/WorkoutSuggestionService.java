package rp.fitkit.api.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import rp.fitkit.api.exception.ResourceNotFoundException;
import rp.fitkit.api.model.*;
import reactor.core.publisher.Mono;
import rp.fitkit.api.model.user.UserSettings;
import rp.fitkit.api.repository.*;
import rp.fitkit.api.repository.user.UserSettingsRepository;

import java.util.ArrayList;
import java.util.List;
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
    private static final double DEFAULT_RPE_LOW   = 6.0;
    private static final double DEFAULT_RPE_HIGH  = 9.0;

    // --- Repositories ---
    private final ExerciseSessionRepository exerciseSessionRepository;
    private final ExerciseTemplateRepository exerciseTemplateRepository;
    private final SetLogRepository setLogRepository;
    private final WorkoutTemplateRepository workoutTemplateRepository;
    private final WorkoutPlanRepository planRepository;
    private final UserSettingsRepository userSettingsRepository;


    public Mono<WorkoutSuggestion> getSuggestionForPlannedExercise(String userId, String exerciseTemplateId) {
        final String logPrefix = String.format("[PLANNED_SUGGESTION] userId=%s, templateId=%s:", userId, exerciseTemplateId);
        log.info("{} START - Ophalen van geplande suggestie gestart.", logPrefix);

        return exerciseTemplateRepository.findById(exerciseTemplateId)
                .doOnNext(et -> log.debug("{} Stap 1/4 - Oefening-template gevonden: {}", logPrefix, "todo get exercise name from et"))
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("{} Fout: Oefening-template met ID '{}' niet gevonden.", logPrefix, exerciseTemplateId);
                    return Mono.error(new ResourceNotFoundException("Geplande oefening niet gevonden (ID: " + exerciseTemplateId + ')'));
                }))
                .flatMap(et -> {
                    log.debug("{} Stap 2/4 - Verifiëren van eigendom...", logPrefix);
                    return workoutTemplateRepository.findById(et.getWorkoutTemplateId())
                            .flatMap(wt -> planRepository.findById(wt.getWorkoutPlanId())
                                    .flatMap(plan -> {
                                        if (!plan.getUserId().equals(userId)) {
                                            log.error("{} Fout: Toegang geweigerd. Plan '{}' (ID: {}) behoort toe aan user '{}', niet aan de aanvrager.", logPrefix, plan.getName(), plan.getId(), plan.getUserId());
                                            return Mono.error(new AccessDeniedException("Geen toegang tot dit workout-plan"));
                                        }
                                        log.debug("{} Eigendom geverifieerd voor plan '{}'.", logPrefix, plan.getName());
                                        return Mono.just(et);
                                    }));
                })
                .flatMap(et -> {
                    log.debug("{} Stap 3/4 - Ophalen van parameters en laatste sessie.", logPrefix);
                    String exerciseName = "todo get exercise name from et";
                    int minR = et.getTargetRepsMin() != null ? et.getTargetRepsMin() : DEFAULT_MIN_REPS;
                    int maxR = et.getTargetRepsMax() != null ? et.getTargetRepsMax() : DEFAULT_MAX_REPS;
                    int tgtS = et.getTargetSets() != null ? et.getTargetSets() : DEFAULT_TARGET_SETS;

                    log.debug("{} Parameters: minReps={}, maxReps={}, targetSets={}", logPrefix, minR, maxR, tgtS);

                    return Mono.zip(
                                    getLastSessionWithSets(userId, exerciseName),
                                    getUserSettings(userId)
                            )
                            .map(t -> {
                                log.debug("{} Stap 4/4 - Suggestie bouwen met sessie- en settings-data.", logPrefix);
                                return buildSuggestion(exerciseName, t.getT1(), minR, maxR, tgtS, t.getT2());
                            })
                            .defaultIfEmpty(createDefaultSuggestion(exerciseName, "Omdat er geen vorige sessie is voor deze oefening."));
                })
                .doOnSuccess(s -> log.info("{} SUCCES - Suggestie succesvol aangemaakt.", logPrefix))
                .doOnError(e -> log.error("{} Fout: De suggestie-pipeline is mislukt.", logPrefix, e));
    }

    public Mono<WorkoutSuggestion> getSuggestionForAdHocExercise(String userId, String exerciseName) {
        final String logPrefix = String.format("[ADHOC_SUGGESTION] userId=%s, exercise=%s:", userId, exerciseName);
        log.info("{} START - Ophalen van ad-hoc suggestie gestart.", logPrefix);

        return Mono.zip(
                        getLastSessionWithSets(userId, exerciseName),
                        getUserSettings(userId)
                )
                .map(t -> {
                    log.debug("{} Suggestie bouwen met sessie- en settings-data.", logPrefix);
                    return buildSuggestion(exerciseName, t.getT1(), DEFAULT_MIN_REPS, DEFAULT_MAX_REPS, DEFAULT_TARGET_SETS, t.getT2());
                })
                .defaultIfEmpty(createDefaultSuggestion(exerciseName, "Omdat er geen vorige sessie is voor deze oefening."))
                .doOnSuccess(s -> log.info("{} SUCCES - Suggestie succesvol aangemaakt.", logPrefix))
                .doOnError(e -> log.error("{} Fout: De suggestie-pipeline is mislukt.", logPrefix, e));
    }


    private Mono<ExerciseSession> getLastSessionWithSets(String userId, String exerciseName) {
        final String logPrefix = String.format("[SESSION_HELPER] userId=%s, exercise=%s:", userId, exerciseName);
        log.debug("{} Ophalen van laatste sessie gestart.", logPrefix);

        return exerciseSessionRepository
                .findByUserIdAndExerciseNameOrderByDateDesc(userId, exerciseName)
                .next()
                .doOnNext(sess -> log.debug("{} Sessie gevonden met ID: {}", logPrefix, sess.getId()))
                .flatMap(sess -> setLogRepository.findByExerciseSessionId(sess.getId())
                        .collectList()
                        .map(sets -> {
                            sess.setSets(sets);
                            log.debug("{} {} sets geladen voor sessie {}.", logPrefix, sets.size(), sess.getId());
                            return sess;
                        }))
                .switchIfEmpty(Mono.defer(() -> {
                    log.info("{} Geen eerdere sessie gevonden.", logPrefix);
                    return Mono.empty();
                }));
    }

    private Mono<UserSettings> getUserSettings(String userId) {
        final String logPrefix = String.format("[SETTINGS_HELPER] userId=%s:", userId);
        log.debug("{} Ophalen van gebruikersinstellingen gestart.", logPrefix);

        return userSettingsRepository.findById(userId)
                .doOnNext(s -> log.debug("{} Instellingen gevonden: {}", logPrefix, s))
                .defaultIfEmpty(new UserSettings())
                .doOnNext(s -> {
                    if (s.getUserId() == null) {
                        log.debug("{} Geen instellingen-record gevonden, standaardwaarden worden toegepast.", logPrefix);
                    }
                });
    }

    protected WorkoutSuggestion applyDoubleProgressionLogic(
            String               exerciseName,
            ExerciseSession      lastSession,
            int                  minReps,
            int                  maxReps,
            int                  targetSets,
            ProgressionStrategy  strategy,
            double               rpeLow,
            double               rpeHigh) {
        final String logPrefix = String.format("[ALGORITME] exercise=%s:", exerciseName);
        log.info("{} Start van double progression algoritme.", logPrefix);
        log.debug("{} Input: minReps={}, maxReps={}, targetSets={}, strategy={}, rpeLow={}, rpeHigh={}", logPrefix, minReps, maxReps, targetSets, strategy, rpeLow, rpeHigh);

        if (lastSession == null || lastSession.getSets().isEmpty()) {
            log.warn("{} Geen geldige laatste sessie gevonden. Fallback naar default suggestie.", logPrefix);
            return createDefaultSuggestion(exerciseName, "De vorige sessie was leeg of niet gevonden.");
        }

        log.debug("{} Aantal sets in vorige sessie: {}", logPrefix, lastSession.getSets().size());
        var validSets = lastSession.getSets().stream()
                .filter(s -> s.getReps() > 0 && s.getWeight() > 0 && s.getRpe() >= 0)
                .collect(Collectors.toList());

        if (validSets.isEmpty()) {
            log.warn("{} De vorige sessie bevatte geen geldige sets (reps/weight/rpe > 0). Fallback naar default suggestie.", logPrefix);
            return createDefaultSuggestion(exerciseName, "De vorige sessie bevatte geen geldige sets.");
        }
        log.debug("{} {}/{} sets zijn geldig en worden gebruikt voor de berekening.", logPrefix, validSets.size(), lastSession.getSets().size());

        double lastWeight = validSets.get(0).getWeight();
        double avgRpe = validSets.stream().mapToInt(SetLog::getRpe).average().orElse(0.0);
        long setsAtMaxReps = validSets.stream().filter(s -> s.getReps() >= maxReps).count();
        boolean repTargetMet = setsAtMaxReps >= targetSets;

        log.info("{} Analyse resultaten: lastWeight={}, avgRpe={}, sets@maxReps={}/{}, repTargetMet={}", logPrefix, lastWeight, avgRpe, setsAtMaxReps, targetSets, repTargetMet);

        String message;

        if (repTargetMet) {
            log.debug("{} Beslissingspad: Herhalingsdoel gehaald.", logPrefix);
            if (avgRpe >= rpeHigh) {
                log.info("{} CONCLUSIE: Consolideren. Doel gehaald, maar RPE (avg {:.1f}) was hoog (grens: {}). Gewicht blijft gelijk.", logPrefix, avgRpe, rpeHigh);
                message = String.format("Rep-doel (%d) gehaald maar het was zwaar (gem. RPE %.1f). Probeer dit gewicht (%.1f kg) te consolideren.", maxReps, avgRpe, lastWeight);
                return new WorkoutSuggestion(exerciseName, repeatSets(targetSets, minReps, lastWeight), message);
            } else {
                double newWeight = lastWeight + WEIGHT_INCREMENT;
                log.info("{} CONCLUSIE: Progressie. Doel gehaald en RPE (avg {:.1f}) was prima. Gewicht wordt verhoogd naar {} kg.", logPrefix, avgRpe, newWeight);
                message = String.format("Top! Rep-doel gehaald. Verhoog het gewicht naar %.1f kg en mik op %d herhalingen.", newWeight, minReps);
                return new WorkoutSuggestion(exerciseName, repeatSets(targetSets, minReps, newWeight), message);
            }
        } else {
            log.debug("{} Beslissingspad: Herhalingsdoel NIET gehaald.", logPrefix);
            boolean feltLight = avgRpe <= rpeLow;
            log.debug("{} Analyse: voelde het licht aan? {} (avgRpe={:.1f}, grens={})", logPrefix, feltLight, avgRpe, rpeLow);

            if (feltLight && strategy == ProgressionStrategy.INTENSITY_FIRST) {
                double newWeight = lastWeight + WEIGHT_INCREMENT;
                log.info("{} CONCLUSIE: Intensiteit verhogen. Training was licht en strategie is INTENSITY_FIRST. Gewicht omhoog naar {} kg.", logPrefix, newWeight);
                message = String.format("Voelde licht aan (gem. RPE %.1f). Verhoog het gewicht naar %.1f kg en reset de herhalingen naar %d.", avgRpe, newWeight, minReps);
                return new WorkoutSuggestion(exerciseName, repeatSets(targetSets, minReps, newWeight), message);
            } else if (feltLight) { // Impliciet: VOLUME_FIRST
                int newReps = Math.min(maxReps, minReps + 1);
                log.info("{} CONCLUSIE: Volume verhogen. Training was licht en strategie is VOLUME_FIRST. Herhalingen omhoog naar {}.", logPrefix, newReps);
                message = String.format("Voelde licht aan (gem. RPE %.1f). Verhoog het aantal herhalingen naar %d op %.1f kg.", avgRpe, newReps, lastWeight);
                return new WorkoutSuggestion(exerciseName, repeatSets(targetSets, newReps, lastWeight), message);
            } else {
                int currentReps = validSets.get(0).getReps();
                log.info("{} CONCLUSIE: Stabiliseren. RPE was niet laag genoeg voor progressie. Blijf op hetzelfde gewicht en herhalingen.", logPrefix);
                message = String.format("Werk verder op %.1f kg. Probeer de vorige prestatie (%d reps) te verbeteren en toe te werken naar %d reps.", lastWeight, currentReps, maxReps);
                return new WorkoutSuggestion(exerciseName, repeatSets(targetSets, currentReps, lastWeight), message);
            }
        }
    }

    /** Helper: bouw een lijst van identieke sets. */
    private List<SetLog> repeatSets(int sets, int reps, double weight) {
        return java.util.stream.IntStream.range(0, sets)
                .mapToObj(i -> new SetLog(null, reps, weight, 0))
                .toList();
    }

    private WorkoutSuggestion createDefaultSuggestion(String exerciseName, String reason) {
        log.warn("[DEFAULT_SUGGESTION] exercise={}: Een standaard suggestie wordt aangemaakt. Reden: {}", exerciseName, reason);
        var list = List.of(
                new SetLog(null, 10, 20.0, 0),
                new SetLog(null, 10, 20.0, 0),
                new SetLog(null, 10, 20.0, 0)
        );
        return new WorkoutSuggestion(
                exerciseName,
                new ArrayList<>(list),
                "Start met deze basis voor %s. Log je training om de volgende keer betere suggesties te krijgen!".formatted(exerciseName)
        );
    }

    private WorkoutSuggestion buildSuggestion(String exerciseName, ExerciseSession lastSession, int minReps, int maxReps, int targetSets, UserSettings settings) {
        ProgressionStrategy strat = (settings.getProgressionStrategy() != null) ? settings.getProgressionStrategy() : ProgressionStrategy.VOLUME_FIRST;
        double rpeLow = (settings.getRpeLowOverride() != null) ? settings.getRpeLowOverride() : DEFAULT_RPE_LOW;
        double rpeHigh = (settings.getRpeHighOverride() != null) ? settings.getRpeHighOverride() : DEFAULT_RPE_HIGH;

        return applyDoubleProgressionLogic(exerciseName, lastSession, minReps, maxReps, targetSets, strat, rpeLow, rpeHigh);
    }
}
