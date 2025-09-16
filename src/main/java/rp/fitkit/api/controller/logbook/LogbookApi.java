package rp.fitkit.api.controller.logbook;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Page;
import reactor.core.publisher.Mono;
import rp.fitkit.api.dto.logbook.*;
import rp.fitkit.api.model.root.SectionType;
import rp.fitkit.api.model.user.User;

import java.time.LocalDate;

@Tag(name = "Logbook", description = "Endpoints for managing the user's daily logbook.")
@SecurityRequirement(name = "bearerAuth")
public interface LogbookApi {

    /**
     * Fetches a paginated list of logbook previews for the authenticated user.
     * <p>
     * This method retrieves historical logbook entries within a specified date range,
     * returning a paginated result. If no date range is provided, it defaults to the
     * last 7 days.
     *
     * @param user         The authenticated user, injected by the security context.
     * @param languageCode The preferred language for response messages (e.g., 'en-GB', 'nl-NL').
     * @param startDate    The start date for the filter (inclusive), in YYYY-MM-DD format.
     * @param endDate      The end date for the filter (inclusive), in YYYY-MM-DD format.
     * @param page         The page number to retrieve (0-indexed).
     * @param size         The number of items to return per page.
     * @param sort         Sorting criteria in the format: property,(asc|desc).
     * @return A {@link Mono} that emits a {@link Page} of {@link LogbookPreviewDto} objects.
     */
    @Operation(
            summary = "Get Logbook History",
            description = """ 
                Fetches a paginated list of logbook previews for the authenticated user.
                Defaults to the last 7 days if no date range is provided.
                """
    )
    Mono<Page<LogbookPreviewDto>> getLogbookHistory(
            @Parameter(hidden = true)
            User user,

            @Parameter(
                    name = "Accept-Language",
                    description = "The preferred language for response messages (e.g., 'en-GB', 'nl-NL').",
                    in = ParameterIn.HEADER,
                    required = true,
                    schema = @Schema(example = "en-GB")
            )
            @NotBlank(message = "Language code cannot be blank")
            String languageCode,

            @Parameter(
                    name = "startDate",
                    description = "The start date for the filter (inclusive), in YYYY-MM-DD format. Defaults to 6 days before the end date.",
                    schema = @Schema(example = "2025-09-10")
            )
            LocalDate startDate,

            @Parameter(
                    name = "endDate",
                    description = "The end date for the filter (inclusive), in YYYY-MM-DD format. Defaults to the current date.",
                    schema = @Schema(example = "2025-09-16")
            )
            LocalDate endDate,

            @Parameter(
                    name = "page",
                    description = "The page number to retrieve (0-indexed).",
                    schema = @Schema(example = "0", defaultValue = "0")
            )
            int page,

            @Parameter(
                    name = "size",
                    description = "The number of items to return per page.",
                    schema = @Schema(example = "10", defaultValue = "10")
            )
            int size,

            @Parameter(
                    name = "sort",
                    description = "Sorting criteria in the format: property,(asc|desc). Default is descending by date.",
                    schema = @Schema(example = "logDate,desc", defaultValue = "logDate,desc")
            )
            String[] sort
    );

    /**
     * Retrieves a single, complete logbook entry for a given date.
     * <p>
     * This method fetches all details for a specific day, including all of its
     * sections, links made to other entries (outgoing), and links from other
     * entries pointing to this one (incoming).
     *
     * @param user         The authenticated user, injected by the security context.
     * @param languageCode The preferred language for response messages (e.g., 'en-GB', 'nl-NL').
     * @param date         The specific date of the logbook entry to retrieve.
     * @return A {@link Mono} that emits the {@link FullLogbookDto} for the given date, or an empty Mono if not found.
     */
    @Operation(
            summary = "Get Logbook Entry by Date",
            description = "Retrieves a single, complete logbook entry for a given date, including all sections, outgoing links, and incoming backlinks."
    )
    Mono<FullLogbookDto> getLogByDate(
            @Parameter(hidden = true)
            User user,

            @Parameter(
                    name = "Accept-Language",
                    description = "The preferred language for response messages (e.g., 'en-GB', 'nl-NL').",
                    in = ParameterIn.HEADER,
                    required = true,
                    schema = @Schema(example = "en-GB")
            )
            @NotBlank(message = "Language code cannot be blank")
            String languageCode,

            @Parameter(
                    name = "date",
                    description = "The date of the logbook entry to retrieve, in YYYY-MM-DD format.",
                    in = ParameterIn.PATH,
                    required = true,
                    schema = @Schema(example = "2025-09-05")
            )
            LocalDate date
    );

    /**
     * Retrieves the complete logbook entry for the current date.
     * <p>
     * This is a convenience method, equivalent to calling {@code getLogByDate} with today's date.
     * It fetches all sections and links for the current day.
     *
     * @param user         The authenticated user, injected by the security context.
     * @param languageCode The preferred language for response messages (e.g., 'en-GB', 'nl-NL').
     * @return A {@link Mono} that emits the {@link FullLogbookDto} for today, or an empty Mono if not found.
     */
    @Operation(
            summary = "Get Today's Logbook Entry",
            description = "A convenience endpoint to retrieve the complete logbook entry for the current date. This is equivalent to calling '/by-date' with today's date."
    )
    Mono<FullLogbookDto> getTodaysLog(
            @Parameter(hidden = true)
            User user,

            @Parameter(
                    name = "Accept-Language",
                    description = "The preferred language for response messages (e.g., 'en-GB', 'nl-NL').",
                    in = ParameterIn.HEADER,
                    required = true,
                    schema = @Schema(example = "en-GB")
            )
            @NotBlank(message = "Language code cannot be blank")
            String languageCode
    );

    /**
     * Creates or updates a section for a specific logbook date.
     * <p>
     * This operation is idempotent. If a section of the specified type already
     * exists for the given date, it will be updated. Otherwise, a new logbook entry
     * and/or section will be created.
     *
     * @param user         The authenticated user.
     * @param languageCode The preferred language for response messages.
     * @param date         The date of the logbook entry to modify.
     * @param sectionType  The type of the logbook section to save.
     * @param request      The request body containing the new summary and mood.
     * @return A {@link Mono} that emits the saved {@link LogSectionDto}.
     */
    @Operation(
            summary = "Create or Update a Logbook Section by Date",
            description = "Creates or updates a section (e.g., 'MORNING') for a specific date. If a log for that date doesn't exist, it will be created automatically."
    )
    Mono<LogSectionDto> saveOrUpdateSection(
            @Parameter(hidden = true)
            User user,

            @Parameter(
                    name = "Accept-Language",
                    in = ParameterIn.HEADER,
                    required = true,
                    schema = @Schema(example = "en-GB")
            )
            @NotBlank(message = "Language code cannot be blank")
            String languageCode,

            @Parameter(
                    name = "date",
                    description = "The date of the logbook entry, in YYYY-MM-DD format.",
                    in = ParameterIn.PATH,
                    required = true,
                    schema = @Schema(example = "2025-09-04")
            )
            LocalDate date,

            @Parameter(
                    name = "sectionType",
                    description = "The type of logbook section to create or update.",
                    in = ParameterIn.PATH,
                    required = true,
                    schema = @Schema(example = "MORNING")
            )
            SectionType sectionType,

            @Valid
            SaveLogSectionRequest request
    );

    /**
     * Generates a graph of interconnected keywords from the user's logbook.
     * <p>
     * This method creates a "concept map" where:
     * <ul>
     * <li><b>Nodes</b> are the unique keywords (anchor texts) used in links.</li>
     * <li><b>Node Weight</b> is the total number of times a keyword is used.</li>
     * <li><b>Edges</b> represent a co-occurrence of two keywords in the same daily log.</li>
     * <li><b>Edge Weight</b> is the number of times two keywords appear together.</li>
     * </ul>
     * This data is ideal for creating a visual graph of related ideas.
     *
     * @param user         The authenticated user, injected by the security context.
     * @param languageCode The preferred language for response messages (e.g., 'en-GB', 'nl-NL').
     * @return A {@link Mono} that emits the {@link GraphDataDto} for the keyword graph.
     */
    @Operation(
            summary = "Get Keyword Graph Data",
            description = "Retrieves all linked keywords as nodes and their co-occurrence in logs as edges, forming a 'concept map'."
    )
    Mono<GraphDataDto> getKeywordGraphData(
            @Parameter(hidden = true)
            User user,

            @Parameter(
                    name = "Accept-Language",
                    description = "The preferred language for response messages (e.g., 'en-GB', 'nl-NL').",
                    in = ParameterIn.HEADER,
                    required = true,
                    schema = @Schema(example = "en-GB")
            )
            @NotBlank(message = "Language code cannot be blank")
            String languageCode
    );
}
