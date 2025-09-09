package rp.fitkit.api.dto.logbook;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import rp.fitkit.api.model.root.SectionType;

import java.time.LocalDate;

/**
 * A generic Data Transfer Object for representing a preview of any linked entity,
 * including context about where the link originates from within the current logbook view.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LinkPreviewDto {

    // --- For Outgoing Links: The specific source within the current log ---
    /**
     * For an outgoing link, this specifies which section it comes from (e.g., MORNING).
     * This will be null for incoming links.
     */
    private SectionType sourceSectionType;

    // --- The Link itself ---
    /**
     * The text in the original summary that formed the link, e.g., "project".
     */
    private String anchorText;


    // --- Describes the OTHER side of the link (the target for outgoing, the source for incoming) ---
    /**
     * The type of the remote entity (e.g., "DAILY_LOG", "PERSON").
     */
    private String remoteEntityType;

    /**
     * For an incoming link from a LogSection, this specifies its type (e.g., EVENING).
     * This will be null for outgoing links or other incoming link types.
     */
    private SectionType remoteSectionType;

    /**
     * The unique ID of the remote entity.
     */
    private String remoteEntityId;

    /**
     * The main title for the preview card.
     * For a log, this is the date. For a person, this is their full name.
     */
    private String remoteTitle;

    /**
     * A short, relevant snippet of text for the preview.
     * For a log, this is a piece of the summary. For a person, this is their bio.
     */
    private String remoteSnippet;

    /**
     * A direct REST API URL to fetch the full details of the remote entity.
     * Example: "/api/logbook/2025-09-04" or "/api/persons/uuid-goes-here"
     */
    private String remoteApiUrl;
}