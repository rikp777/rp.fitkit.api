package rp.fitkit.api.repository.mental;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import rp.fitkit.api.model.mental.MentalHealthStep;

@Repository
public interface MentalHealthStepRepository extends R2dbcRepository<MentalHealthStep, Long> {
    Mono<MentalHealthStep> findByStepNumber(int stepNumber);
}
