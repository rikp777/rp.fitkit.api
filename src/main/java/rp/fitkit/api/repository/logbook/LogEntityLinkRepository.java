package rp.fitkit.api.repository.logbook;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import rp.fitkit.api.model.logbook.LogEntityLink;
import rp.fitkit.api.model.root.EntityType;

import java.util.List;
import java.util.UUID;


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
     * Finds all links that originate from a specific type of source entity.
     * Used in the graph view to find all potential edges.
     *
     * @param sourceEntityType The type of the source entity (e.g., LOG_SECTION).
     * @return A Flux of matching entity links.
     */
    Flux<LogEntityLink> findBySourceEntityType(EntityType sourceEntityType); // <-- Added

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
     * Finds all links that point to a specific set of target entities.
     * Used in the graph view to calculate the weight of nodes.
     *
     * @param targetEntityType The type of the target entity (e.g., DAILY_LOG).
     * @param targetEntityIds  A list of target entity IDs.
     * @return A Flux of matching entity links.
     */
    Flux<LogEntityLink> findByTargetEntityTypeAndTargetEntityIdIn(EntityType targetEntityType, List<String> targetEntityIds); // <-- Added


    /**
     * Deletes all links that originate from a specific source entity.
     * This is used to clear old links before saving updated ones when a log section is edited.
     *
     * @param sourceEntityType The type of the source entity (e.g., LOG_SECTION).
     * @param sourceEntityId   The ID of the source entity.
     * @return A Mono that completes when the deletion is done.
     */
    Mono<Void> deleteAllBySourceEntityTypeAndSourceEntityId(EntityType sourceEntityType, String sourceEntityId);

    /**
     * Finds all links that originate from any log section belonging to a specific user.
     * This is an efficient way to gather all links for a user for graph generation.
     *
     * @param userId The ID of the user.
     * @return A Flux of all matching entity links.
     */
    @Query("SELECT l.* FROM log_entity_links l " +
            "JOIN log_sections s ON l.source_entity_id = s.section_id::text " +
            "JOIN daily_logs d ON s.log_id = d.log_id " +
            "WHERE d.user_id = :userId AND l.source_entity_type = 'LOG_SECTION'")
    Flux<LogEntityLink> findAllByUserId(UUID userId);
}
