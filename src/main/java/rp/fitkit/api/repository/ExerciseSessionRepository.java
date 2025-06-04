package rp.fitkit.api.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import rp.fitkit.api.model.ExerciseSession;

public interface ExerciseSessionRepository extends R2dbcRepository<ExerciseSession, String> {
    Flux<ExerciseSession> findByUserIdAndExerciseNameOrderByDateDesc(String userId, String exerciseName);
    Flux<ExerciseSession> findByUserIdOrderByDateDesc(String userId);
}
