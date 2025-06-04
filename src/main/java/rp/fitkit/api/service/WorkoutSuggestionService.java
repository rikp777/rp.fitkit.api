package rp.fitkit.api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import rp.fitkit.api.dto.ExerciseLogDto;
import rp.fitkit.api.model.ExerciseSession;
import rp.fitkit.api.model.SetLog;
import rp.fitkit.api.model.WorkoutSuggestion;
import reactor.core.publisher.Mono;
import rp.fitkit.api.repository.ExerciseSessionRepository;
import rp.fitkit.api.repository.SetLogRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WorkoutSuggestionService {

    private final ExerciseSessionRepository exerciseSessionRepository;
    private final SetLogRepository setLogRepository;

    @Autowired
    public WorkoutSuggestionService(
            ExerciseSessionRepository exerciseSessionRepository,
            SetLogRepository setLogRepository
            /*, UserRepository userRepository */
    ) {
        this.exerciseSessionRepository = exerciseSessionRepository;
        this.setLogRepository = setLogRepository;
        // this.userRepository = userRepository;
    }

    public Mono<WorkoutSuggestion> generateSuggestionReactive(String userId, String exerciseName) {
        return exerciseSessionRepository.findByUserIdAndExerciseNameOrderByDateDesc(userId, exerciseName)
                .next()
                .flatMap(session ->

                        setLogRepository.findByExerciseSessionId(session.getId())
                                .collectList()
                                .map(sets -> {
                                    session.setSets(sets);
                                    return session;
                                })
                )
                .map(sessionWithSets -> {
                    List<SetLog> suggestedSets = new ArrayList<>();
                    String message = "Suggestie voor " + exerciseName + " gebaseerd op je training van " + sessionWithSets.getDate() + ":";
                    for (SetLog lastSet : sessionWithSets.getSets()) {
                        // SIMPEL ALGORITME (versie 0.1): +2.5kg, zelfde reps
                        suggestedSets.add(new SetLog(null, lastSet.getReps(), lastSet.getWeight() + 2.5));
                    }
                    return new WorkoutSuggestion(exerciseName, suggestedSets, message);
                })
                .defaultIfEmpty(createDefaultSuggestion(exerciseName));
    }

    private WorkoutSuggestion createDefaultSuggestion(String exerciseName) {
        List<SetLog> suggestedSets = new ArrayList<>();
        suggestedSets.add(new SetLog(null, 10, 20.0));
        suggestedSets.add(new SetLog(null, 10, 20.0));
        suggestedSets.add(new SetLog(null, 10, 20.0));
        String message = "Start met deze basis voor " + exerciseName + ". Log je training om betere suggesties te krijgen!";
        return new WorkoutSuggestion(exerciseName, suggestedSets, message);
    }

    /**
     * Logt een nieuwe workout sessie (ExerciseSession en bijbehorende SetLogs)
     * @param userId De ID van de gebruiker die de workout logt.
     * @param logDto De DTO met de workout details.
     * @return Een Mono die de opgeslagen ExerciseSession bevat, inclusief de opgeslagen sets.
     */
    public Mono<ExerciseSession> logWorkoutSessionReactive(String userId, ExerciseLogDto logDto) {
        ExerciseSession newSession = new ExerciseSession();
        newSession.setUserId(userId);
        newSession.setExerciseName(logDto.getExerciseName());
        if (logDto.getDate() != null) {
            newSession.setDate(logDto.getDate());
        }
        newSession.setNotes(logDto.getNotes());

        return exerciseSessionRepository.save(newSession)
                .flatMap(savedSession -> {

                    List<SetLog> setLogsToSave = new ArrayList<>();
                    if (logDto.getSets() != null) {
                        setLogsToSave = logDto.getSets().stream()
                                .map(setDto -> new SetLog(savedSession.getId(), setDto.getReps(), setDto.getWeight()))
                                .collect(Collectors.toList());
                    }


                    return setLogRepository.saveAll(setLogsToSave)
                            .collectList()
                            .map(savedSets -> {
                                savedSession.setSets(savedSets);
                                return savedSession;
                            });
                });
    }

}
