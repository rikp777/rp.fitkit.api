package rp.fitkit.api.repository.logbook;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import rp.fitkit.api.model.logbook.LogEntityLink;
import rp.fitkit.api.model.root.EntityType;

import java.util.List;

/**
 * Repository for managing {@link LogEntityLink} entities.
 * This interface handles all database operations for the generic linking system.
 */
@Repository
public interface LogEntityLinkRepository extends R2dbcRepository<LogEntityLink, Long> {

    /**
     * Finds all links that originate from a specific set of source entities.
     * This is used to find all outgoing links from a daily log's sections.
     *
     * @param sourceEntityType The type of the source entity (e.g., LOG_SECTION).
     * @param sourceEntityIds  A list of source entity IDs.
     * @return A Flux of matching entity links.
     */
    Flux<LogEntityLink> findBySourceEntityTypeAndSourceEntityIdIn(EntityType sourceEntityType, List<String> sourceEntityIds);

    /**
     * Finds all links that point to a specific target entity.
     * This is used to find all incoming "backlinks" to a daily log.
     *
     * @param targetEntityType The type of the target entity (e.g., DAILY_LOG).
     * @param targetEntityId   The ID of the target entity.
     * @return A Flux of matching entity links.
     */
    Flux<LogEntityLink> findByTargetEntityTypeAndTargetEntityId(EntityType targetEntityType, String targetEntityId);

    /**
     * Deletes all links that originate from a specific source entity.
     * This is used to clear old links before saving updated ones when a log section is edited.
     *
     * @param sourceEntityType The type of the source entity (e.g., LOG_SECTION).
     * @param sourceEntityId   The ID of the source entity.
     * @return A Mono that completes when the deletion is done.
     */
    Mono<Void> deleteAllBySourceEntityTypeAndSourceEntityId(EntityType sourceEntityType, String sourceEntityId);
}

