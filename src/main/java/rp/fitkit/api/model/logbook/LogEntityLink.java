package rp.fitkit.api.model.logbook;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import rp.fitkit.api.model.root.EntityType;

/**
 * Represents a generic, polymorphic link between two entities in the application.
 * The source and target can be of any type defined in the EntityType enum.
 */
@Data
@Table("log_entity_links")
public class LogEntityLink {
    @Id
    @Column("link_id")
    private Long id;

    /**
     * The type of the entity from which the link originates.
     * E.g., LOG_SECTION, EXERCISE.
     */
    @Column("source_entity_type")
    private EntityType sourceEntityType;

    /**
     * The unique identifier of the source entity.
     * Stored as a String to accommodate both numeric IDs and UUIDs.
     */
    @Column("source_entity_id")
    private String sourceEntityId;

    /**
     * The user-visible text for the link, e.g., the text between brackets in Markdown.
     */
    @Column("anchor_text")
    private String anchorText;

    /**
     * The type of the entity to which the link points.
     * E.g., DAILY_LOG, PERSON.
     */
    @Column("target_entity_type")
    private EntityType targetEntityType;

    /**
     * The unique identifier of the target entity.
     * Stored as a String to accommodate both numeric IDs and UUIDs.
     */
    @Column("target_entity_id")
    private String targetEntityId;
}
