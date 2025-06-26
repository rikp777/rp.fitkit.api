package rp.fitkit.api.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import rp.fitkit.api.dto.ExerciseSessionResponseDto;
import rp.fitkit.api.model.ExerciseSession;
import rp.fitkit.api.repository.ExerciseSessionRepository;
import rp.fitkit.api.repository.SetLogRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class WorkoutHistoryService {

    private final ExerciseSessionRepository exerciseSessionRepository;
    private final SetLogRepository setLogRepository;

    public Mono<Map<LocalDate, List<ExerciseSessionResponseDto>>> getFullHistoryGroupedByDate(String userId) {
        return exerciseSessionRepository.findByUserIdOrderByDateDesc(userId)
                .flatMap(this::hydrateSessionToDto)
                .collectList()
                .map(allSessions -> allSessions.stream()
                        .collect(Collectors.groupingBy(ExerciseSessionResponseDto::getDate))
                );
    }

    public Flux<ExerciseSessionResponseDto> getHistoryForExercise(String userId, String exerciseName) {
        return exerciseSessionRepository.findByUserIdAndExerciseNameOrderByDateDesc(userId, exerciseName)
                .flatMap(this::hydrateSessionToDto);
    }

    private Mono<ExerciseSessionResponseDto> hydrateSessionToDto(ExerciseSession session) {
        return setLogRepository.findByExerciseSessionId(session.getId())
                .collectList()
                .map(sets -> new ExerciseSessionResponseDto(
                        session.getId(), "todo get exercise name", session.getDate(),
                        session.getNotes(), sets
                ));
    }
}
