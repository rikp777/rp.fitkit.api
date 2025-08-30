package rp.fitkit.api.repository.mental;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import rp.fitkit.api.model.mental.PerformedAction;

@Repository
public interface PerformedActionRepository extends R2dbcRepository<PerformedAction, Long> {
    Flux<PerformedAction> findByUserId(String userId);
}

