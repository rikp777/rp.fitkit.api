package rp.fitkit.api.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import rp.fitkit.api.model.exercise.ExerciseSession;

public interface ExerciseSessionRepository extends R2dbcRepository<ExerciseSession, String> {
    @Query("""
        SELECT es.* FROM exercise_session es
        JOIN exercise e ON es.exercise_id = e.id
        JOIN exercise_translation et ON e.id = et.exercise_id
        WHERE es.user_id = :userId AND et.name = :exerciseName
        ORDER BY es.session_date DESC
    """)
    Flux<ExerciseSession> findByUserIdAndExerciseNameOrderByDateDesc(String userId, String exerciseName);
    Flux<ExerciseSession> findByUserIdOrderByDateDesc(String userId);
}
