package rp.fitkit.api.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import rp.fitkit.api.model.WorkoutPlan;

public interface WorkoutPlanRepository extends R2dbcRepository<WorkoutPlan, String> {
    Flux<WorkoutPlan> findByUserId(String userId);
}
