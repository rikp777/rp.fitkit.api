package rp.fitkit.api.repository.logbook;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import rp.fitkit.api.model.logbook.LogSection;
import rp.fitkit.api.model.root.SectionType;

import java.util.Collection;

@Repository
public interface LogSectionRepository extends R2dbcRepository<LogSection, Long> {
    Flux<LogSection> findByDailyLogId(Long logId);
    Mono<LogSection> findByDailyLogIdAndSectionType(Long logId, SectionType sectionType);
    Flux<LogSection> findByDailyLogIdIn(Collection<Long> logIds);
}


