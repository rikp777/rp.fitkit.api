package rp.fitkit.api.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import rp.fitkit.api.model.exercise.ExerciseTemplate;

public interface ExerciseTemplateRepository extends R2dbcRepository<ExerciseTemplate, String> {
    Flux<ExerciseTemplate> findByWorkoutTemplateIdOrderByDisplayOrderAsc(String workoutTemplateId);
}

