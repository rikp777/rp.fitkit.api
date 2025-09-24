package rp.fitkit.api.repository.logbook;

import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import rp.fitkit.api.model.root.DailyLog;

import java.time.LocalDate;
import java.util.Collection;
import java.util.UUID;

@Repository
public interface DailyLogRepository extends R2dbcRepository<DailyLog, Long> {
    Flux<DailyLog> findByUserId(UUID userId, Sort sort);
    Mono<DailyLog> findByUserIdAndLogDate(UUID userId, LocalDate date);
    Flux<DailyLog> findByUserIdIn(Collection<UUID> userIds, Sort sort);
    Mono<Long> countByUserId(UUID userId);
    Mono<Long> countByUserIdIn(Collection<UUID> userIds);
    Flux<DailyLog> findByUserIdAndLogDateBetween(UUID userId, LocalDate startDate, LocalDate endDate, Sort sort);
    Mono<Long> countByUserIdAndLogDateBetween(UUID userId, LocalDate startDate, LocalDate endDate);
}

