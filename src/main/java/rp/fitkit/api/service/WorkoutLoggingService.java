package rp.fitkit.api.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import rp.fitkit.api.dto.ExerciseLogDto;
import rp.fitkit.api.dto.ExerciseSessionResponseDto;
import rp.fitkit.api.exception.ResourceNotFoundException;
import rp.fitkit.api.model.exercise.ExerciseSession;
import rp.fitkit.api.model.SetLog;
import rp.fitkit.api.repository.ExerciseSessionRepository;
import rp.fitkit.api.repository.SetLogRepository;
import rp.fitkit.api.repository.user.UserRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class WorkoutLoggingService {

    private final UserRepository userRepository;
    private final ExerciseSessionRepository exerciseSessionRepository;
    private final SetLogRepository setLogRepository;
    private final Sinks.Many<ExerciseSessionResponseDto> workoutSessionSink;

    @Transactional
    public Mono<ExerciseSession> logWorkoutSession(String userId, ExerciseLogDto logDto) {
        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Gebruiker met ID '" + userId + "' niet gevonden.")))
                .flatMap(user -> {
                    ExerciseSession newSession = new ExerciseSession(userId);
                    if (logDto.getDate() != null) newSession.setDate(logDto.getDate());
                    newSession.setNotes(logDto.getNotes());
                    return exerciseSessionRepository.save(newSession);
                })
                .flatMap(savedSession -> {
                    List<SetLog> setLogsToSave = new ArrayList<>();
                    if (logDto.getSets() != null) {
                        setLogsToSave = logDto.getSets().stream()
                                .map(dto -> new SetLog(savedSession.getId(), dto.getReps(), dto.getWeight(), dto.getRpe()))
                                .toList();
                    }
                    return setLogRepository.saveAll(setLogsToSave)
                            .collectList()
                            .map(savedSets -> {
                                savedSession.setSets(savedSets);
                                workoutSessionSink.tryEmitNext(new ExerciseSessionResponseDto(
                                        savedSession.getId(), "bleb", savedSession.getDate(),
                                        savedSession.getNotes(), savedSets
                                ));
                                return savedSession;
                            });
                });
    }
}
