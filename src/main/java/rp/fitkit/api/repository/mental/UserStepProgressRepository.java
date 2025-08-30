package rp.fitkit.api.repository.mental;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import rp.fitkit.api.model.mental.UserStepProgress;

@Repository
public interface UserStepProgressRepository extends R2dbcRepository<UserStepProgress, Long> {
    Flux<UserStepProgress> findByUserId(String userId);
    Mono<UserStepProgress> findByUserIdAndMentalHealthStepId(String userId, Long mentalHealthStepId);
}

