package rp.fitkit.api.repository.logbook;

import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import rp.fitkit.api.model.root.DailyLog;

import java.time.LocalDate;
import java.util.Collection;

@Repository
public interface DailyLogRepository extends R2dbcRepository<DailyLog, Long> {
    Flux<DailyLog> findByUserId(String userId, Sort sort);
    Mono<DailyLog> findByUserIdAndLogDate(String userId, LocalDate date);
    Flux<DailyLog> findByUserIdIn(Collection<String> userIds, Sort sort);
    Mono<Long> countByUserId(String userId);
    Mono<Long> countByUserIdIn(Collection<String> userIds);
    Flux<DailyLog> findByUserIdAndLogDateBetween(String userId, LocalDate startDate, LocalDate endDate, Sort sort);
    Mono<Long> countByUserIdAndLogDateBetween(String userId, LocalDate startDate, LocalDate endDate);
}

