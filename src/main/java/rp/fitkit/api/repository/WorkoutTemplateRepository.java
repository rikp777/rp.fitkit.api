package rp.fitkit.api.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import rp.fitkit.api.model.WorkoutTemplate;

import java.util.UUID;

public interface WorkoutTemplateRepository extends R2dbcRepository<WorkoutTemplate, UUID> {
    Flux<WorkoutTemplate> findByWorkoutPlanId(UUID workoutPlanId);
}
