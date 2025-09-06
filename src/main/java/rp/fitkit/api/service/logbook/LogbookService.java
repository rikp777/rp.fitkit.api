package rp.fitkit.api.service.logbook;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import rp.fitkit.api.dto.logbook.FullLogbookDto;
import rp.fitkit.api.dto.logbook.LinkPreviewDto;
import rp.fitkit.api.dto.logbook.LogSectionDto;
import rp.fitkit.api.dto.logbook.LogbookPreviewDto;
import rp.fitkit.api.model.logbook.LogLink;
import rp.fitkit.api.model.logbook.LogSection;
import rp.fitkit.api.model.root.DailyLog;
import rp.fitkit.api.model.root.SectionType;
import rp.fitkit.api.repository.logbook.DailyLogRepository;
import rp.fitkit.api.repository.logbook.LogLinkRepository;
import rp.fitkit.api.repository.logbook.LogSectionRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LogbookService {

    private static final Pattern LINK_PATTERN = Pattern.compile("\\[([^\\]]+)\\]\\(log:(\\d+)\\)");

    private final DailyLogRepository dailyLogRepository;
    private final LogSectionRepository logSectionRepository;
    private final LogLinkRepository logLinkRepository;

    private final TransactionalOperator transactionalOperator;

    public Mono<Page<LogbookPreviewDto>> getPaginatedLogbooksForUser(String userId, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        log.info("Fetching paginated logbooks for user: {}, from: {}, to: {}", userId, startDate, endDate);
        Mono<Long> totalMono = dailyLogRepository.countByUserIdAndLogDateBetween(userId, startDate, endDate);
        Flux<DailyLog> logsFlux = dailyLogRepository.findByUserIdAndLogDateBetween(userId, startDate, endDate, pageable.getSort());

        return totalMono.flatMap(total ->
                logsFlux.skip(pageable.getOffset())
                        .take(pageable.getPageSize())
                        .collectList()
                        .doOnSuccess(dailyLogs -> log.debug("Found {} daily logs on current page for user {}", dailyLogs.size(), userId))
                        .flatMap(dailyLogs -> {
                            if (dailyLogs.isEmpty()) {
                                return Mono.just(new PageImpl<>(List.of(), pageable, total));
                            }

                            List<Long> logIds = dailyLogs.stream().map(DailyLog::getId).collect(Collectors.toList());

                            return logSectionRepository.findByDailyLogIdIn(logIds)
                                    .collect(Collectors.groupingBy(LogSection::getDailyLogId))
                                    .map(sectionsByLogId -> {
                                        List<LogbookPreviewDto> previews = dailyLogs.stream()
                                                .map(log -> {
                                                    List<LogSection> sections = sectionsByLogId.getOrDefault(log.getId(), List.of());
                                                    String preview = sections.stream()
                                                            .map(LogSection::getSummary)
                                                            .filter(s -> s != null && !s.isBlank())
                                                            .findFirst()
                                                            .map(this::createSummaryPreview)
                                                            .orElse("");
                                                    return new LogbookPreviewDto(log.getId(), log.getLogDate(), preview);
                                                })
                                                .collect(Collectors.toList());
                                        log.info("Successfully created page with {} logbook previews for user {}", previews.size(), userId);
                                        return new PageImpl<>(previews, pageable, total);
                                    });
                        })
        );
    }


    /**
     * Zoekt een daglog voor een gebruiker op een specifieke datum, of maakt een nieuwe aan als deze niet bestaat.
     */
    public Mono<DailyLog> findOrCreateDailyLog(String userId, LocalDate date) {
        log.debug("Attempting to find or create daily log for user: {} on date: {}", userId, date);

        return dailyLogRepository.findByUserIdAndLogDate(userId, date)
                .doOnSuccess(dailyLog -> {
                    if (dailyLog != null) {
                        log.debug("Found existing daily log with ID: {} for user: {}", dailyLog.getId(), userId);
                    }
                })
                .switchIfEmpty(Mono.defer(() -> {
                    log.info("No existing daily log found for user: {} on date: {}. Creating a new one.", userId, date);
                    DailyLog newLog = new DailyLog();
                    newLog.setUserId(userId);
                    newLog.setLogDate(date);
                    return dailyLogRepository
                            .save(newLog)
                            .doOnSuccess(savedLog -> log.info("Successfully created new daily log with ID: {}", savedLog.getId()));
                }));
    }


    /**
     * Slaat een sectie van een daglog op of werkt deze bij.
     */
    @Transactional
    public Mono<LogSection> saveLogSection(Long dailyLogId, SectionType sectionType, String summary, String mood) {
        return logSectionRepository.findByDailyLogIdAndSectionType(dailyLogId, sectionType)
                .switchIfEmpty(Mono.defer(() -> {
                    log.debug("No existing section found... Creating new section.");
                    LogSection newSection = new LogSection();
                    newSection.setDailyLogId(dailyLogId);
                    newSection.setSectionType(sectionType);
                    return logSectionRepository.save(newSection)
                            .onErrorResume(DataIntegrityViolationException.class, e -> {
                                // This is the reminder for your future self!
                                log.error("""
                                    ####################################################################################################
                                    # DATABASE SEQUENCE ERROR DETECTED
                                    # --------------------------------------------------------------------------------------------------
                                    # Failed to create a new LogSection because of a data integrity violation.
                                    # This is almost certainly because the database sequence for 'log_sections' is out of sync.
                                    # This happens after manually inserting test data with explicit IDs.
                                    #
                                    # TO FIX: Synchronize the sequence in your Liquibase seeding script, like so:
                                    # <sql>
                                    #   SELECT setval('log_sections_section_id_seq', (SELECT MAX(section_id) FROM log_sections));
                                    # </sql>
                                    ####################################################################################################
                                    """, e);
                                return Mono.error(e);
                            });
                }))
                .flatMap(section -> {
                    section.setSummary(summary);
                    section.setMood(mood);
                    return logSectionRepository.save(section);
                })
                .flatMap(savedSection -> {
                    // Re-create the links
                    return logLinkRepository.deleteAllBySourceSectionId(savedSection.getId())
                            .then(parseAndSaveLinks(savedSection));
                });
    }

    public Flux<LogSection> getSectionsForLog(Long logId) {
        log.debug("Fetching sections for logId: {}", logId);

        return logSectionRepository.findByDailyLogId(logId);
    }

    public Mono<FullLogbookDto> getFullLogbook(String userId, LocalDate date) {
        log.info("Fetching full logbook for user: {} on date: {}", userId, date);
        return findOrCreateDailyLog(userId, date)
                .flatMap(dailyLog -> {
                    Mono<List<LogSectionDto>> sectionsMono = getSectionsForLog(dailyLog.getId())
                            .map(s -> new LogSectionDto(s.getSectionType(), s.getSummary(), s.getMood()))
                            .collectList();

                    // OUTGOING = previews based on TARGET content
                    Mono<List<LinkPreviewDto>> outgoingLinksMono = getSectionsForLog(dailyLog.getId())
                            .map(LogSection::getId).collectList()
                            .flatMapMany(ids -> ids.isEmpty() ? Flux.empty() : logLinkRepository.findBySourceSectionIdIn(ids))
                            .flatMap(this::createOutgoingLinkPreview)
                            .collectList();

                    // INCOMING = backlinks, previews based on SOURCE context
                    Mono<List<LinkPreviewDto>> incomingLinksMono = logLinkRepository.findByTargetLogId(dailyLog.getId())
                            .flatMap(this::createIncomingLinkPreview)
                            .collectList();

                    return Mono.zip(sectionsMono, outgoingLinksMono, incomingLinksMono)
                            .doOnSuccess(tuple -> log.info(
                                    "Assembled full logbook for logId: {}. Found {} sections, {} outgoing links, {} incoming links.",
                                    dailyLog.getId(), tuple.getT1().size(), tuple.getT2().size(), tuple.getT3().size()))
                            .map(tuple -> new FullLogbookDto(
                                    dailyLog.getId(),
                                    dailyLog.getLogDate(),
                                    tuple.getT1(),
                                    tuple.getT2(),
                                    tuple.getT3()
                            ));
                });
    }

    private Mono<LogSection> parseAndSaveLinks(LogSection section) {
        log.debug("Parsing links for sectionId: {}", section.getId());
        Matcher matcher = LINK_PATTERN.matcher(section.getSummary());
        List<LogLink> linksToSave = new ArrayList<>();
        while (matcher.find()) {
            LogLink link = new LogLink();
            link.setSourceSectionId(section.getId());
            link.setAnchorText(matcher.group(1));
            link.setTargetLogId(Long.parseLong(matcher.group(2)));
            linksToSave.add(link);
        }

        log.debug("Found {} links to save for sectionId: {}", linksToSave.size(), section.getId());
        if (linksToSave.isEmpty()) {
            return Mono.just(section);
        }
        linksToSave.forEach(link ->
                log.info("Attempting to save LogLink with values: sourceSectionId={}, anchorText='{}', targetLogId={}",
                        link.getSourceSectionId(), link.getAnchorText(), link.getTargetLogId())
        );
        return logLinkRepository.saveAll(linksToSave)
                .doOnComplete(() -> log.debug("Successfully saved {} links for sectionId: {}", linksToSave.size(), section.getId()))
                .then(Mono.just(section));
    }

    private Mono<LinkPreviewDto> createLinkPreview(LogLink link) {
        log.debug("Creating link preview for linkId: {}", link.getId());
        Mono<LogSection> sourceSectionMono = logSectionRepository.findById(link.getSourceSectionId());
        Mono<DailyLog> sourceLogMono = sourceSectionMono.flatMap(s -> dailyLogRepository.findById(s.getDailyLogId()));
        Mono<DailyLog> targetLogMono = dailyLogRepository.findById(link.getTargetLogId());

        return Mono.zip(sourceSectionMono, sourceLogMono, targetLogMono)
                .map(tuple -> {
                    LogSection sourceSection = tuple.getT1();
                    DailyLog sourceLog = tuple.getT2();
                    DailyLog targetLog = tuple.getT3();
                    String snippet = generateSnippet(sourceSection.getSummary(), link.getAnchorText());

                    return new LinkPreviewDto(
                            sourceLog.getId(),
                            sourceLog.getLogDate(),
                            sourceSection.getSectionType(),
                            link.getAnchorText(),
                            snippet,
                            targetLog.getId(),
                            targetLog.getLogDate()
                    );
                });
    }

    private String generateSnippet(String summary, String anchorText) {
        int index = summary.indexOf(anchorText);
        if (index == -1) {
            return anchorText;
        }

        // Simpele implementatie: neem 30 karakters voor en na.
        int start = Math.max(0, index - 100);
        int end = Math.min(summary.length(), index + anchorText.length() + 100);

        String snippet = summary.substring(start, end);
        if (start > 0) snippet = "..." + snippet;
        if (end < summary.length()) snippet = snippet + "...";

        return snippet;
    }

    private String createSummaryPreview(String summary) {
        final int maxLength = 100;
        if (summary == null || summary.length() <= maxLength) {
            return summary;
        }

        // Zoek de laatste spatie vóór de maximale lengte om te voorkomen dat woorden worden afgekapt.
        int lastSpace = summary.lastIndexOf(' ', maxLength);
        if (lastSpace > 0) {
            return summary.substring(0, lastSpace) + "...";
        } else {
            // Fallback voor het geval er geen spaties zijn in de eerste 100 tekens.
            return summary.substring(0, maxLength) + "...";
        }
    }

    private Mono<LinkPreviewDto> createOutgoingLinkPreview(LogLink link) {
        log.debug("Creating OUTGOING link preview for linkId: {}", link.getId());

        // Source is still needed for metadata in LinkPreviewDto (e.g. section type)
        Mono<LogSection> sourceSectionMono = logSectionRepository.findById(link.getSourceSectionId());
        Mono<DailyLog> sourceLogMono = sourceSectionMono.flatMap(s -> dailyLogRepository.findById(s.getDailyLogId()));
        Mono<DailyLog> targetLogMono = dailyLogRepository.findById(link.getTargetLogId());

        // Compute snippet FROM TARGET
        Mono<String> targetSnippetMono = bestTargetSnippet(link.getTargetLogId(), link.getAnchorText());

        return Mono.zip(sourceSectionMono, sourceLogMono, targetLogMono, targetSnippetMono)
                .map(tuple -> {
                    LogSection sourceSection = tuple.getT1();
                    DailyLog sourceLog = tuple.getT2();
                    DailyLog targetLog = tuple.getT3();
                    String snippet = tuple.getT4();

                    return new LinkPreviewDto(
                            sourceLog.getId(),
                            sourceLog.getLogDate(),
                            sourceSection.getSectionType(),
                            link.getAnchorText(),
                            snippet,                // << TARGET-based snippet
                            targetLog.getId(),
                            targetLog.getLogDate()
                    );
                });
    }

    // INCOMING (backlinks): show SOURCE snippets (your current behavior)
    private Mono<LinkPreviewDto> createIncomingLinkPreview(LogLink link) {
        log.debug("Creating INCOMING link preview for linkId: {}", link.getId());

        Mono<LogSection> sourceSectionMono = logSectionRepository.findById(link.getSourceSectionId());
        Mono<DailyLog> sourceLogMono = sourceSectionMono.flatMap(s -> dailyLogRepository.findById(s.getDailyLogId()));
        Mono<DailyLog> targetLogMono = dailyLogRepository.findById(link.getTargetLogId());

        return Mono.zip(sourceSectionMono, sourceLogMono, targetLogMono)
                .map(tuple -> {
                    LogSection sourceSection = tuple.getT1();
                    DailyLog sourceLog = tuple.getT2();
                    DailyLog targetLog = tuple.getT3();

                    String snippet = generateSnippet(sourceSection.getSummary(), link.getAnchorText());

                    return new LinkPreviewDto(
                            sourceLog.getId(),
                            sourceLog.getLogDate(),
                            sourceSection.getSectionType(),
                            link.getAnchorText(),
                            snippet,                // << SOURCE-based snippet (backlinks)
                            targetLog.getId(),
                            targetLog.getLogDate()
                    );
                });
    }

    /**
     * Builds a target-centric snippet:
     * 1) Try to find the anchor text in any section of the target log and return a contextual snippet.
     * 2) Otherwise, pick the first non-blank section summary as a preview of the target.
     * 3) Fall back to the anchor text if the target has no usable content.
     */
    private Mono<String> bestTargetSnippet(Long targetLogId, String anchorText) {
        return logSectionRepository.findByDailyLogId(targetLogId)
                .collectList()
                .map(sections -> {
                    if (sections.isEmpty()) {
                        return anchorText; // nothing to show on target
                    }

                    // Prefer a section that actually mentions the anchor text
                    for (LogSection s : sections) {
                        String summary = s.getSummary();
                        if (summary != null && !summary.isBlank()) {
                            int idx = summary.indexOf(anchorText);
                            if (idx >= 0) {
                                return generateSnippet(summary, anchorText);
                            }
                        }
                    }

                    // Otherwise, first non-blank summary (trimmed)
                    for (LogSection s : sections) {
                        String summary = s.getSummary();
                        if (summary != null && !summary.isBlank()) {
                            return createSummaryPreview(summary);
                        }
                    }

                    // Absolute fallback
                    return anchorText;
                });
    }
}