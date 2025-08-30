//package rp.fitkit.api.service;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.security.access.AccessDeniedException;
//import reactor.core.publisher.Flux;
//import reactor.core.publisher.Mono;
//import reactor.test.StepVerifier;
//import rp.fitkit.api.exception.ResourceNotFoundException;
//import rp.fitkit.api.model.*;
//import rp.fitkit.api.repository.*;
//
//import java.time.LocalDate;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//import java.util.stream.IntStream;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.anyString;
//import static org.mockito.Mockito.*;
//
//
//@ExtendWith(MockitoExtension.class)
//class WorkoutSuggestionServiceTest {
//
//    @Mock private ExerciseSessionRepository exerciseSessionRepository;
//    @Mock private ExerciseTemplateRepository exerciseTemplateRepository;
//    @Mock private SetLogRepository           setLogRepository;
//    @Mock private WorkoutTemplateRepository  workoutTemplateRepository;
//    @Mock private WorkoutPlanRepository      planRepository;
//    @Mock private UserSettingsRepository     userSettingsRepository;
//
//    @InjectMocks
//    private WorkoutSuggestionService workoutSuggestionService;
//
//    private static final String USER_ID = "test-user-123";
//    private static final String PLAN_ID = "plan-abc-456";
//    private static final String WORKOUT_TEMPLATE_ID = "wt-def-789";
//    private static final String EXERCISE_TEMPLATE_ID = "et-ghi-012";
//    private static final String SESSION_ID = "sess-jkl-345";
//    private static final String EXERCISE_NAME = "Barbell Squat";
//    private static final double WEIGHT_INCREMENT = 2.5;
//
//    private ExerciseTemplate exerciseTemplate;
//    private WorkoutTemplate workoutTemplate;
//    private WorkoutPlan workoutPlan;
//    private UserSettings defaultUserSettings;
//
//    @BeforeEach
//    void setup() {
//        reset(exerciseSessionRepository,
//                exerciseTemplateRepository,
//                setLogRepository,
//                workoutTemplateRepository,
//                planRepository,
//                userSettingsRepository);
//
//        workoutPlan = new WorkoutPlan();
//        workoutPlan.setId(PLAN_ID);
//        workoutPlan.setUserId(USER_ID);
//        workoutPlan.setName("Strength Plan");
//
//        workoutTemplate = new WorkoutTemplate();
//        workoutTemplate.setId(WORKOUT_TEMPLATE_ID);
//        workoutTemplate.setWorkoutPlanId(PLAN_ID);
//
//        exerciseTemplate = new ExerciseTemplate();
//        exerciseTemplate.setId(EXERCISE_TEMPLATE_ID);
//        exerciseTemplate.setWorkoutTemplateId(WORKOUT_TEMPLATE_ID);
////        exerciseTemplate.setExerciseName(EXERCISE_NAME);
//        exerciseTemplate.setTargetSets(3);
//        exerciseTemplate.setTargetRepsMin(8);
//        exerciseTemplate.setTargetRepsMax(12);
//
//        defaultUserSettings = new UserSettings();
//        defaultUserSettings.setUserId(USER_ID);
//        defaultUserSettings.setProgressionStrategy(ProgressionStrategy.VOLUME_FIRST);
//
//        lenient().when(userSettingsRepository.findById(anyString())).thenReturn(Mono.just(defaultUserSettings));
//    }
//
//    private void mockOwnershipChain() {
//        when(exerciseTemplateRepository.findById(EXERCISE_TEMPLATE_ID)).thenReturn(Mono.just(exerciseTemplate));
//        when(workoutTemplateRepository.findById(WORKOUT_TEMPLATE_ID)).thenReturn(Mono.just(workoutTemplate));
//        when(planRepository.findById(PLAN_ID)).thenReturn(Mono.just(workoutPlan));
//    }
//
//    private void mockLastSession(List<SetLog> sets) {
//        ExerciseSession session = new ExerciseSession(USER_ID, EXERCISE_NAME, LocalDate.now(), sets, "");
//        session.setId(SESSION_ID);
//        when(exerciseSessionRepository.findByUserIdAndExerciseNameOrderByDateDesc(USER_ID, EXERCISE_NAME))
//                .thenReturn(Flux.just(session));
//        when(setLogRepository.findByExerciseSessionId(SESSION_ID)).thenReturn(Flux.fromIterable(sets));
//    }
//
//    private void mockNoPreviousSession() {
//        when(exerciseSessionRepository.findByUserIdAndExerciseNameOrderByDateDesc(anyString(), anyString()))
//                .thenReturn(Flux.empty());
//    }
//
//    @Test
//    @DisplayName("Should throw ResourceNotFoundException if exercise template does not exist")
//    void getSuggestionForPlanned_shouldThrowResourceNotFound_whenTemplateMissing() {
//        when(exerciseTemplateRepository.findById(anyString())).thenReturn(Mono.empty());
//
//        Mono<WorkoutSuggestion> mono = workoutSuggestionService.getSuggestionForPlannedExercise(USER_ID, "unknown-id");
//
//        StepVerifier.create(mono)
//                .expectError(ResourceNotFoundException.class)
//                .verify();
//    }
//
//    @Test
//    @DisplayName("Should throw AccessDeniedException if user does not own the workout plan")
//    void getSuggestionForPlanned_shouldThrowAccessDenied_whenNotOwner() {
//        workoutPlan.setUserId("another-user-id"); // Set a different owner
//        mockOwnershipChain();
//
//        Mono<WorkoutSuggestion> mono = workoutSuggestionService.getSuggestionForPlannedExercise(USER_ID, EXERCISE_TEMPLATE_ID);
//
//        StepVerifier.create(mono)
//                .expectError(AccessDeniedException.class)
//                .verify();
//    }
//
//    @Test
//    @DisplayName("Should return default suggestion if no previous session exists (Ad-Hoc)")
//    void getSuggestionForAdHoc_shouldReturnDefault_whenNoHistory() {
//        mockNoPreviousSession();
//        // Mock no settings to test default UserSettings object creation
//        when(userSettingsRepository.findById(USER_ID)).thenReturn(Mono.empty());
//
//        Mono<WorkoutSuggestion> result = workoutSuggestionService.getSuggestionForAdHocExercise(USER_ID, EXERCISE_NAME);
//
//        StepVerifier.create(result)
//                .assertNext(suggestion -> {
//                    assertNotNull(suggestion);
//                    assertEquals(EXERCISE_NAME, suggestion.getExerciseName());
//                    assertEquals(3, suggestion.getSuggestedSets().size());
//                    assertEquals(10, suggestion.getSuggestedSets().get(0).getReps());
//                    assertEquals(20.0, suggestion.getSuggestedSets().get(0).getWeight());
//                })
//                .verifyComplete();
//    }
//
//    @Test
//    @DisplayName("Should return default suggestion for PLANNED exercise if no previous session exists")
//    void getSuggestionForPlanned_shouldReturnDefault_whenNoHistory() {
//        mockOwnershipChain();
//        mockNoPreviousSession();
//
//        Mono<WorkoutSuggestion> result = workoutSuggestionService.getSuggestionForPlannedExercise(USER_ID, EXERCISE_TEMPLATE_ID);
//
//        StepVerifier.create(result)
//                .assertNext(suggestion -> {
//                    assertEquals(EXERCISE_NAME, suggestion.getExerciseName());
//                    assertEquals(3, suggestion.getSuggestedSets().size());
//                    assertEquals(10, suggestion.getSuggestedSets().get(0).getReps());
//                    assertEquals(20.0, suggestion.getSuggestedSets().get(0).getWeight());
//                })
//                .verifyComplete();
//    }
//
//    @Test
//    @DisplayName("Should return default suggestion if previous session has no valid sets")
//    void getSuggestion_shouldReturnDefault_whenSessionHasInvalidSets() {
//        List<SetLog> invalidSets = List.of(
//                new SetLog(SESSION_ID, 0, 100.0, 8), // Invalid reps
//                new SetLog(SESSION_ID, 10, 0, 8),   // Invalid weight
//                new SetLog(SESSION_ID, 10, 100.0, -1) // Invalid RPE
//        );
//        mockLastSession(invalidSets);
//        mockOwnershipChain();
//
//        Mono<WorkoutSuggestion> result = workoutSuggestionService.getSuggestionForPlannedExercise(USER_ID, EXERCISE_TEMPLATE_ID);
//
//        StepVerifier.create(result)
//                .assertNext(suggestion -> {
//                    assertEquals(3, suggestion.getSuggestedSets().size());
//                    assertEquals(10, suggestion.getSuggestedSets().get(0).getReps()); // Default reps
//                    assertEquals(20.0, suggestion.getSuggestedSets().get(0).getWeight()); // Default weight
//                })
//                .verifyComplete();
//    }
//
//    @Test
//    @DisplayName("Should return default suggestion when previous session has no sets")
//    void getSuggestionForAdHoc_shouldReturnDefault_whenSessionHasNoSets() {
//        mockLastSession(Collections.emptyList());
//
//        Mono<WorkoutSuggestion> result = workoutSuggestionService.getSuggestionForAdHocExercise(USER_ID, EXERCISE_NAME);
//
//        StepVerifier.create(result)
//                .assertNext(suggestion -> {
//                    assertEquals(EXERCISE_NAME, suggestion.getExerciseName());
//                    assertEquals(3, suggestion.getSuggestedSets().size());
//                    assertEquals(10, suggestion.getSuggestedSets().get(0).getReps());
//                    assertEquals(20.0, suggestion.getSuggestedSets().get(0).getWeight());
//                })
//                .verifyComplete();
//    }
//
//
//    // ===================================================================================
//    // ==                          Progression Logic Tests                              ==
//    // ===================================================================================
//
//    @Test
//    @DisplayName("Progression: Increase weight when rep goal is met and RPE is not high")
//    void progression_shouldIncreaseWeight_whenRepsMetAndRpeOk() {
//        double lastWeight = 100.0;
//        List<SetLog> lastSessionSets = List.of(
//                new SetLog(SESSION_ID, 12, lastWeight, 8),
//                new SetLog(SESSION_ID, 12, lastWeight, 8),
//                new SetLog(SESSION_ID, 12, lastWeight, 8)
//        );
//        mockOwnershipChain();
//        mockLastSession(lastSessionSets);
//
//        Mono<WorkoutSuggestion> result = workoutSuggestionService.getSuggestionForPlannedExercise(USER_ID, EXERCISE_TEMPLATE_ID);
//
//        StepVerifier.create(result)
//                .assertNext(suggestion -> {
//                    assertEquals(3, suggestion.getSuggestedSets().size());
//                    suggestion.getSuggestedSets().forEach(set -> {
//                        assertEquals(8, set.getReps()); // Resets to minReps
//                        assertEquals(lastWeight + WEIGHT_INCREMENT, set.getWeight()); // Incremented weight
//                    });
//                })
//                .verifyComplete();
//    }
//
//    @Test
//    @DisplayName("Progression: Consolidate weight when rep goal is met and RPE is high")
//    void progression_shouldConsolidateWeight_whenRepsMetAndRpeHigh() {
//        double lastWeight = 100.0;
//        List<SetLog> lastSessionSets = List.of(
//                new SetLog(SESSION_ID, 12, lastWeight, 9),
//                new SetLog(SESSION_ID, 12, lastWeight, 10),
//                new SetLog(SESSION_ID, 12, lastWeight, 9)
//        );
//        mockOwnershipChain();
//        mockLastSession(lastSessionSets);
//
//        Mono<WorkoutSuggestion> result = workoutSuggestionService.getSuggestionForPlannedExercise(USER_ID, EXERCISE_TEMPLATE_ID);
//
//        StepVerifier.create(result)
//                .assertNext(suggestion -> {
//                    assertEquals(3, suggestion.getSuggestedSets().size());
//                    suggestion.getSuggestedSets().forEach(set -> {
//                        assertEquals(8, set.getReps()); // Resets to minReps
//                        assertEquals(lastWeight, set.getWeight()); // Same weight
//                    });
//                })
//                .verifyComplete();
//    }
//
//    @Test
//    @DisplayName("Progression: Stabilize when rep goal not met and RPE is not low")
//    void progression_shouldStabilize_whenRepsNotMetAndRpeNormal() {
//        double lastWeight = 100.0;
//        int currentReps = 10;
//        List<SetLog> lastSessionSets = List.of(
//                new SetLog(SESSION_ID, currentReps, lastWeight, 7),
//                new SetLog(SESSION_ID, 9, lastWeight, 8),
//                new SetLog(SESSION_ID, 8, lastWeight, 8)
//        );
//        mockOwnershipChain();
//        mockLastSession(lastSessionSets);
//
//        Mono<WorkoutSuggestion> result = workoutSuggestionService.getSuggestionForPlannedExercise(USER_ID, EXERCISE_TEMPLATE_ID);
//
//        StepVerifier.create(result)
//                .assertNext(suggestion -> {
//                    assertEquals(3, suggestion.getSuggestedSets().size());
//                    suggestion.getSuggestedSets().forEach(set -> {
//                        assertEquals(currentReps, set.getReps()); // Stabilize at current reps
//                        assertEquals(lastWeight, set.getWeight()); // Same weight
//                    });
//                })
//                .verifyComplete();
//    }
//
//    @Test
//    @DisplayName("Progression: Increase reps when goal not met, RPE is low, and strategy is VOLUME_FIRST")
//    void progression_shouldIncreaseReps_withVolumeFirstStrategy() {
//        double lastWeight = 100.0;
//        // Mock user settings for this specific strategy
//        UserSettings settings = new UserSettings();
//        settings.setProgressionStrategy(ProgressionStrategy.VOLUME_FIRST);
//        when(userSettingsRepository.findById(USER_ID)).thenReturn(Mono.just(settings));
//
//        List<SetLog> lastSessionSets = List.of(
//                new SetLog(SESSION_ID, 9, lastWeight, 5), // Reps not met, RPE is low
//                new SetLog(SESSION_ID, 9, lastWeight, 6),
//                new SetLog(SESSION_ID, 8, lastWeight, 6)
//        );
//        mockOwnershipChain();
//        mockLastSession(lastSessionSets);
//
//        Mono<WorkoutSuggestion> result = workoutSuggestionService.getSuggestionForPlannedExercise(USER_ID, EXERCISE_TEMPLATE_ID);
//
//        StepVerifier.create(result)
//                .assertNext(suggestion -> {
//                    assertEquals(3, suggestion.getSuggestedSets().size());
//                    suggestion.getSuggestedSets().forEach(set -> {
//                        assertEquals(exerciseTemplate.getTargetRepsMin() + 1, set.getReps()); // minReps + 1
//                        assertEquals(lastWeight, set.getWeight()); // Same weight
//                    });
//                })
//                .verifyComplete();
//    }
//
//    @Test
//    @DisplayName("Progression: Increase weight when goal not met, RPE is low, and strategy is INTENSITY_FIRST")
//    void progression_shouldIncreaseWeight_withIntensityFirstStrategy() {
//        double lastWeight = 100.0;
//        // Mock user settings for this specific strategy
//        UserSettings settings = new UserSettings();
//        settings.setProgressionStrategy(ProgressionStrategy.INTENSITY_FIRST);
//        when(userSettingsRepository.findById(USER_ID)).thenReturn(Mono.just(settings));
//
//        List<SetLog> lastSessionSets = List.of(
//                new SetLog(SESSION_ID, 9, lastWeight, 5), // Reps not met, RPE is low
//                new SetLog(SESSION_ID, 9, lastWeight, 6),
//                new SetLog(SESSION_ID, 8, lastWeight, 6)
//        );
//        mockOwnershipChain();
//        mockLastSession(lastSessionSets);
//
//        Mono<WorkoutSuggestion> result = workoutSuggestionService.getSuggestionForPlannedExercise(USER_ID, EXERCISE_TEMPLATE_ID);
//
//        StepVerifier.create(result)
//                .assertNext(suggestion -> {
//                    assertEquals(3, suggestion.getSuggestedSets().size());
//                    suggestion.getSuggestedSets().forEach(set -> {
//                        assertEquals(exerciseTemplate.getTargetRepsMin(), set.getReps()); // Reset to minReps
//                        assertEquals(lastWeight + WEIGHT_INCREMENT, set.getWeight()); // Incremented weight
//                    });
//                })
//                .verifyComplete();
//    }
//
//    @Test
//    @DisplayName("Progression: Should use RPE overrides from UserSettings when present")
//    void progression_shouldUseRpeOverridesFromSettings() {
//        double lastWeight = 100.0;
//
//        UserSettings customSettings = new UserSettings();
//        customSettings.setProgressionStrategy(ProgressionStrategy.VOLUME_FIRST);
//        customSettings.setRpeLowOverride(7.0); // Higher RPE threshold for "low"
//        customSettings.setRpeHighOverride(9.5); // Higher RPE threshold for "high"
//        when(userSettingsRepository.findById(USER_ID)).thenReturn(Mono.just(customSettings));
//
//        // Average RPE is 7.33, which is now considered "low" by the override
//        List<SetLog> lastSessionSets = List.of(
//                new SetLog(SESSION_ID, 9, lastWeight, 7),
//                new SetLog(SESSION_ID, 8, lastWeight, 7),
//                new SetLog(SESSION_ID, 8, lastWeight, 8)
//        );
//        mockOwnershipChain();
//        mockLastSession(lastSessionSets);
//
//        // Since RPE is "low" and strategy is VOLUME_FIRST, we expect an increase in reps
//        Mono<WorkoutSuggestion> result = workoutSuggestionService.getSuggestionForPlannedExercise(USER_ID, EXERCISE_TEMPLATE_ID);
//
//        StepVerifier.create(result)
//                .assertNext(suggestion -> {
//                    suggestion.getSuggestedSets().forEach(set -> {
//                        assertEquals(exerciseTemplate.getTargetRepsMin() + 1, set.getReps());
//                        assertEquals(lastWeight, set.getWeight());
//                    });
//                })
//                .verifyComplete();
//    }
//
//    @Test
//    @DisplayName("Progression: Should correctly filter out invalid sets and use the valid one")
//    void progression_shouldFilterInvalidSetsAndUseValidOne() {
//        double lastWeight = 50.0;
//        List<SetLog> mixedSets = List.of(
//                new SetLog(SESSION_ID, -1, 0, -1),       // Invalid
//                new SetLog(SESSION_ID, 11, lastWeight, 8), // Valid, but not at max reps
//                new SetLog(SESSION_ID, 10, 0, 8)         // Invalid
//        );
//        mockLastSession(mixedSets);
//
//        // Logic should be based on the single valid set: Reps not met, RPE is not low -> Stabilize
//        Mono<WorkoutSuggestion> result = workoutSuggestionService.getSuggestionForAdHocExercise(USER_ID, EXERCISE_NAME);
//
//        StepVerifier.create(result)
//                .assertNext(suggestion -> {
//                    suggestion.getSuggestedSets().forEach(set -> {
//                        assertEquals(11, set.getReps()); // Stabilize at the valid set's reps
//                        assertEquals(lastWeight, set.getWeight());
//                    });
//                })
//                .verifyComplete();
//    }
//
//    @Test
//    @DisplayName("Should propagate error when any repository in the ownership chain fails")
//    void getSuggestionForPlanned_shouldPropagateError_whenOwnershipCheckFails() {
//        when(exerciseTemplateRepository.findById(EXERCISE_TEMPLATE_ID)).thenReturn(Mono.just(exerciseTemplate));
//
//        when(workoutTemplateRepository.findById(WORKOUT_TEMPLATE_ID))
//                .thenReturn(Mono.error(new RuntimeException("Database connection failed")));
//
//        Mono<WorkoutSuggestion> result = workoutSuggestionService.getSuggestionForPlannedExercise(USER_ID, EXERCISE_TEMPLATE_ID);
//
//        StepVerifier.create(result)
//                .expectErrorMatches(throwable ->
//                        throwable instanceof RuntimeException && "Database connection failed".equals(throwable.getMessage())
//                )
//                .verify();
//    }
//
//    @Test
//    @DisplayName("Should use default constants when exercise template has null targets")
//    void getSuggestionForPlanned_shouldUseDefaults_whenTemplateTargetsAreNull() {
//
//        exerciseTemplate.setTargetSets(null);
//        exerciseTemplate.setTargetRepsMin(null);
//        exerciseTemplate.setTargetRepsMax(null);
//        mockOwnershipChain();
//
//        double lastWeight = 100.0;
//
//        List<SetLog> lastSessionSets = List.of(
//                new SetLog(SESSION_ID, 12, lastWeight, 8),
//                new SetLog(SESSION_ID, 12, lastWeight, 8),
//                new SetLog(SESSION_ID, 12, lastWeight, 8)
//        );
//        mockLastSession(lastSessionSets);
//
//        Mono<WorkoutSuggestion> result = workoutSuggestionService.getSuggestionForPlannedExercise(USER_ID, EXERCISE_TEMPLATE_ID);
//
//        StepVerifier.create(result)
//                .assertNext(suggestion -> {
//                    assertEquals(3, suggestion.getSuggestedSets().size());
//                    suggestion.getSuggestedSets().forEach(set -> {
//                        assertEquals(8, set.getReps());
//                        assertEquals(lastWeight + WEIGHT_INCREMENT, set.getWeight());
//                    });
//                })
//                .verifyComplete();
//    }
//
//    @Test
//    @DisplayName("Should fall back to defaults when UserSettings fields are null")
//    void progression_shouldUseDefaults_whenUserSettingsFieldsAreNull() {
//        // 1. Maak UserSettings aan waar de velden leeg (null) zijn
//        UserSettings settingsWithNulls = new UserSettings();
//        settingsWithNulls.setUserId(USER_ID);
//        settingsWithNulls.setProgressionStrategy(null); // Dit moet terugvallen op de standaard (VOLUME_FIRST)
//        settingsWithNulls.setRpeLowOverride(null);
//        settingsWithNulls.setRpeHighOverride(null);
//        when(userSettingsRepository.findById(USER_ID)).thenReturn(Mono.just(settingsWithNulls));
//
//        double lastWeight = 100.0;
//        // 2. Mock een sessie die de logica voor strategieën activeert (herhalingsdoel niet gehaald, lage RPE)
//        List<SetLog> lastSessionSets = List.of(new SetLog(SESSION_ID, 9, lastWeight, 5));
//        mockLastSession(lastSessionSets);
//
//        // 3. Voer de service-methode uit
//        Mono<WorkoutSuggestion> result = workoutSuggestionService.getSuggestionForAdHocExercise(USER_ID, EXERCISE_NAME);
//
//        // 4. Verifieer het resultaat
//        // De standaardstrategie is VOLUME_FIRST, dus het aantal herhalingen moet omhoog gaan.
//        StepVerifier.create(result)
//                .assertNext(suggestion -> {
//                    suggestion.getSuggestedSets().forEach(set -> {
//                        assertEquals(lastWeight, set.getWeight());
//                        assertEquals(9, set.getReps()); // Standaard min reps (8) + 1
//                    });
//                })
//                .verifyComplete();
//    }
//
//    @Test
//    @DisplayName("Progression: Should not increase weight if number of sets at max reps is less than targetSets")
//    void progression_shouldNotIncreaseWeight_whenNotAllSetsHitMaxReps() {
//        // 1. Stel de doelen in de template in
//        exerciseTemplate.setTargetSets(3);
//        exerciseTemplate.setTargetRepsMax(12);
//        mockOwnershipChain();
//
//        double lastWeight = 100.0;
//        // 2. Mock een sessie waar slechts 2 van de 3 sets het max aantal herhalingen halen.
//        List<SetLog> lastSessionSets = List.of(
//                new SetLog(SESSION_ID, 12, lastWeight, 8), // Gehaald
//                new SetLog(SESSION_ID, 12, lastWeight, 8), // Gehaald
//                new SetLog(SESSION_ID, 11, lastWeight, 9)  // Niet gehaald
//        );
//        mockLastSession(lastSessionSets);
//
//        // 3. Voer de service-methode uit
//        Mono<WorkoutSuggestion> result = workoutSuggestionService.getSuggestionForPlannedExercise(USER_ID, EXERCISE_TEMPLATE_ID);
//
//        // 4. Verifieer het resultaat
//        // Omdat niet alle sets het doel haalden, valt de logica terug op "stabiliseren".
//        // Het gewicht mag dus niet verhoogd worden.
//        StepVerifier.create(result)
//                .assertNext(suggestion -> {
//                    suggestion.getSuggestedSets().forEach(set -> {
//                        assertEquals(lastWeight, set.getWeight()); // Gewicht blijft gelijk
//                        assertEquals(12, set.getReps());          // Herhalingen stabiliseren op die van de eerste set
//                    });
//                })
//                .verifyComplete();
//    }
//}
//
