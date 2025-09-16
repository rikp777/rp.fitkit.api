package rp.fitkit.api.repository.audit;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;
import rp.fitkit.api.dto.audit.Consent;

import java.util.UUID;

public interface ConsentRepository extends ReactiveCrudRepository<Consent, UUID> {
    Mono<Consent> findByJustification(String justification);
}

