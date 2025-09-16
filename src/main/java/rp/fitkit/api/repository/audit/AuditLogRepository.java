package rp.fitkit.api.repository.audit;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import rp.fitkit.api.model.audit.AuditLog;

public interface AuditLogRepository extends ReactiveCrudRepository<AuditLog, Long> {
    Flux<AuditLog> findBySubjectId(String subjectId, Pageable pageable);
    Mono<Long> countBySubjectId(String subjectId);
}

