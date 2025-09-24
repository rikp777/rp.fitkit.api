package rp.fitkit.api.repository.audit;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import rp.fitkit.api.model.audit.AuditLog;

import java.time.LocalDateTime;
import java.util.UUID;

public interface AuditLogRepository extends ReactiveCrudRepository<AuditLog, Long> {
    Flux<AuditLog> findBySubjectId(UUID subjectId, Pageable pageable);
    Mono<Long> countBySubjectId(UUID subjectId);
    Flux<AuditLog> findBySubjectIdAndTimestampBetween(UUID subjectId, LocalDateTime start, LocalDateTime end, Pageable pageable);
    Mono<Long> countBySubjectIdAndTimestampBetween(UUID subjectId, LocalDateTime start, LocalDateTime end);
}


