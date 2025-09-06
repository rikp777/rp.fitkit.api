package rp.fitkit.api.repository;

import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import rp.fitkit.api.model.exercise.ExerciseMuscleGroup;

@Repository
public interface ExerciseMuscleGroupRepository extends R2dbcRepository<ExerciseMuscleGroup, String> {

    @Query("SELECT muscle_group_id FROM exercise_muscle_group WHERE exercise_id = :exerciseId")
    Flux<String> findMuscleGroupIdsByExerciseId(String exerciseId);

    @Modifying
    @Query("DELETE FROM exercise_muscle_group WHERE exercise_id = :exerciseId")
    Mono<Void> deleteByExerciseId(String exerciseId);

    Flux<ExerciseMuscleGroup> findByExerciseId(String exerciseId);
}

