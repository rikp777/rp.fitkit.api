package rp.fitkit.api.repository.mental;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import rp.fitkit.api.model.mental.UserStepProgress;

import java.util.UUID;

@Repository
public interface UserStepProgressRepository extends R2dbcRepository<UserStepProgress, Long> {
    Flux<UserStepProgress> findByUserId(UUID userId);
    Mono<UserStepProgress> findByUserIdAndMentalHealthStepId(UUID userId, Long mentalHealthStepId);
}

