package rp.fitkit.api.repository.logbook;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import rp.fitkit.api.model.logbook.LogLink;

import java.util.Collection;

@Repository
public interface LogLinkRepository extends R2dbcRepository<LogLink, Long> {
    Flux<LogLink> findByTargetLogId(Long targetLogId);
    Flux<LogLink> findBySourceSectionIdIn(Collection<Long> sourceSectionIds);
    Mono<Void> deleteAllBySourceSectionId(Long sourceSectionId);
}

