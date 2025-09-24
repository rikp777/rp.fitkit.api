package rp.fitkit.api.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import rp.fitkit.api.model.WorkoutPlan;

import java.util.UUID;

public interface WorkoutPlanRepository extends R2dbcRepository<WorkoutPlan, UUID> {
    Flux<WorkoutPlan> findByUserId(UUID userId);
}
