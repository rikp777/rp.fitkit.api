package rp.fitkit.api.service.logbook;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import rp.fitkit.api.dto.logbook.*;
import rp.fitkit.api.model.logbook.LogEntityLink;
import rp.fitkit.api.model.logbook.LogSection;
import rp.fitkit.api.model.logbook.Person;
import rp.fitkit.api.model.root.DailyLog;
import rp.fitkit.api.model.root.EntityType;
import rp.fitkit.api.model.root.SectionType;
import rp.fitkit.api.repository.logbook.DailyLogRepository;
import rp.fitkit.api.repository.logbook.LogEntityLinkRepository;
import rp.fitkit.api.repository.logbook.LogSectionRepository;
import rp.fitkit.api.repository.logbook.PersonRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

//todo encrypt logbook data in db with jasypt

@Service
@RequiredArgsConstructor
@Slf4j
public class LogbookService {

    private static final Pattern LINK_PATTERN = Pattern.compile("\\[([^\\]]+)\\]\\(log:(\\d+)\\)");

    private final DailyLogRepository dailyLogRepository;
    private final LogSectionRepository logSectionRepository;
    private final LogEntityLinkRepository logEntityLinkRepository;

    private final PersonRepository personRepository;

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

                                                    List<String> moods = sections.stream()
                                                            .map(LogSection::getMood)
                                                            .filter(mood -> mood != null && !mood.isBlank())
                                                            .distinct()
                                                            .collect(Collectors.toList());

                                                    return new LogbookPreviewDto(log.getId(), log.getLogDate(), preview, moods);
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
                    return logEntityLinkRepository.deleteAllBySourceEntityTypeAndSourceEntityId(
                                    EntityType.LOG_SECTION,
                                    savedSection.getId().toString()
                            )
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
                            .map(section -> section.getId().toString())
                            .collectList()
                            .flatMapMany(sectionIds -> {
                                if (sectionIds.isEmpty()) return Flux.empty();
                                return logEntityLinkRepository.findBySourceEntityTypeAndSourceEntityIdIn(EntityType.LOG_SECTION, sectionIds);
                            })
                            .flatMap(this::createOutgoingLinkPreview)
                            .collectList();

                    // INCOMING = backlinks, previews based on SOURCE context
                    Mono<List<LinkPreviewDto>> incomingLinksMono = logEntityLinkRepository
                            .findByTargetEntityTypeAndTargetEntityId(EntityType.DAILY_LOG, dailyLog.getId().toString())
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

    public Mono<GraphDataDto> getKeywordGraphData(String userId) {
        // 1. Fetch ALL links created by the user. This is our raw data.
        // For now, let's assume a method that can get all links for a user.
        // A custom query as described above would be the most efficient way.
        // We will simulate it by fetching all links for now.
        return logEntityLinkRepository.findAll() // Replace with a more efficient findAllByUserId if possible
                .collectList()
                .flatMap(allLinks -> {

                    // 2. Create NODES and calculate their weights (total occurrences)
                    Map<String, Long> nodeWeights = allLinks.stream()
                            .collect(Collectors.groupingBy(LogEntityLink::getAnchorText, Collectors.counting()));

                    List<GraphNodeDto> nodes = nodeWeights.entrySet().stream()
                            .map(entry -> new GraphNodeDto(entry.getKey(), entry.getKey(), entry.getValue().intValue()))
                            .toList();

                    // 3. To find EDGES, first group links by the DailyLog they belong to.
                    Mono<Map<Long, Long>> sectionToLogMapMono = logSectionRepository.findAll() // Again, should be scoped to user
                            .collectMap(LogSection::getId, LogSection::getDailyLogId);

                    return sectionToLogMapMono.map(sectionToLogMap -> {
                        Map<Long, List<String>> keywordsByLogId = allLinks.stream()
                                .filter(link -> sectionToLogMap.containsKey(Long.parseLong(link.getSourceEntityId())))
                                .collect(Collectors.groupingBy(
                                        link -> sectionToLogMap.get(Long.parseLong(link.getSourceEntityId())),
                                        Collectors.mapping(LogEntityLink::getAnchorText, Collectors.toList())
                                ));

                        // 4. Calculate EDGE weights (how many times keywords appear together)
                        Map<String, Integer> edgeWeights = new HashMap<>();
                        for (List<String> keywordsInLog : keywordsByLogId.values()) {
                            List<String> uniqueKeywords = keywordsInLog.stream().distinct().sorted().toList();
                            if (uniqueKeywords.size() < 2) continue;

                            // Generate all pairs of keywords in this log
                            for (int i = 0; i < uniqueKeywords.size(); i++) {
                                for (int j = i + 1; j < uniqueKeywords.size(); j++) {
                                    // Create a stable key, e.g., "initiatief::project"
                                    String key = uniqueKeywords.get(i) + "::" + uniqueKeywords.get(j);
                                    edgeWeights.put(key, edgeWeights.getOrDefault(key, 0) + 1);
                                }
                            }
                        }

                        // 5. Create the Edge DTOs
                        List<GraphEdgeDto> edges = edgeWeights.entrySet().stream()
                                .map(entry -> {
                                    String[] keys = entry.getKey().split("::");
                                    return new GraphEdgeDto(keys[0], keys[1], entry.getValue());
                                })
                                .toList();

                        return new GraphDataDto(nodes, edges);
                    });
                });
    }
    private Mono<LogSection> parseAndSaveLinks(LogSection section) {
        log.debug("Parsing links for sectionId: {}", section.getId());
        Matcher matcher = LINK_PATTERN.matcher(section.getSummary());
        List<LogEntityLink> linksToSave = new ArrayList<>();

        while (matcher.find()) {
            try {
                String anchorText = matcher.group(1);
                // Converteer de gevonden tekst (bv. "log") naar de juiste enum (bv. EntityType.DAILY_LOG)
                String typeString = matcher.group(2).toUpperCase();
                // We gebruiken DAILY_LOG als het type 'log' is, voor compatibiliteit.
                if ("LOG".equals(typeString)) {
                    typeString = "DAILY_LOG";
                }
                EntityType targetType = EntityType.valueOf(typeString);
                String targetId = matcher.group(3);

                LogEntityLink link = new LogEntityLink();
                link.setSourceEntityType(EntityType.LOG_SECTION);
                link.setSourceEntityId(section.getId().toString());
                link.setAnchorText(anchorText);
                link.setTargetEntityType(targetType);
                link.setTargetEntityId(targetId);

                linksToSave.add(link);
            } catch (IllegalArgumentException e) {
                log.warn("Invalid entity type found in link: '{}'. Skipping.", matcher.group(2));
            }
        }

        log.debug("Found {} valid links to save for sectionId: {}", linksToSave.size(), section.getId());
        if (linksToSave.isEmpty()) {
            return Mono.just(section);
        }
        return logEntityLinkRepository.saveAll(linksToSave)
                .doOnComplete(() -> log.debug("Successfully saved {} links for sectionId: {}", linksToSave.size(), section.getId()))
                .then(Mono.just(section));
    }

    private String generateSnippet(String summary, String anchorText) {
        if (summary == null || anchorText == null) return "...";
        int index = summary.indexOf(anchorText);
        if (index == -1) return createSummaryPreview(summary);

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

    private Mono<LinkPreviewDto> createOutgoingLinkPreview(LogEntityLink link) {
        log.debug("Creating OUTGOING link preview for linkId: {}, type: {}", link.getId(), link.getTargetEntityType());

        // Hier komt de magie: afhankelijk van het type doel, roepen we een andere methode aan.
        switch (link.getTargetEntityType()) {
            case DAILY_LOG: return createDailyLogOutgoingPreview(link);
            case PERSON: return createPersonOutgoingPreview(link);
            default: return createGenericPreview(link);
        }
    }

    private Mono<LinkPreviewDto> createIncomingLinkPreview(LogEntityLink link) {
        log.debug("Creating INCOMING link preview for linkId: {}, type: {}", link.getId(), link.getSourceEntityType());

        switch (link.getSourceEntityType()) {
            case LOG_SECTION: return createLogSectionIncomingPreview(link);
            default: return createGenericPreview(link);
        }
    }

    private Mono<LinkPreviewDto> createGenericPreview(LogEntityLink link) {
        return Mono.just(new LinkPreviewDto(
                null, link.getAnchorText(), "UNKNOWN", null, "0",
                link.getAnchorText(), "Preview not implemented.", null
        ));
    }

    private Mono<LinkPreviewDto> createDailyLogOutgoingPreview(LogEntityLink link) {
        Mono<LogSection> sourceSectionMono = logSectionRepository.findById(Long.parseLong(link.getSourceEntityId()));
        Mono<DailyLog> targetLogMono = dailyLogRepository.findById(Long.parseLong(link.getTargetEntityId()));
        Mono<String> targetSnippetMono = bestTargetSnippetForDailyLog(Long.parseLong(link.getTargetEntityId()), link.getAnchorText());

        return Mono.zip(sourceSectionMono, targetLogMono, targetSnippetMono)
                .map(tuple -> {
                    DailyLog targetLog = tuple.getT2();
                    return new LinkPreviewDto(
                            tuple.getT1().getSectionType(),
                            link.getAnchorText(),
                            link.getTargetEntityType().name(),
                            null,
                            link.getTargetEntityId(),
                            targetLog.getLogDate().toString(),
                            tuple.getT3(),
                            "/api/logbook/by-date/" + targetLog.getLogDate()
                    );
                });
    }

    private Mono<LinkPreviewDto> createPersonOutgoingPreview(LogEntityLink link) {
        Mono<LogSection> sourceSectionMono = logSectionRepository.findById(Long.parseLong(link.getSourceEntityId()));
        Mono<Person> targetPersonMono = personRepository.findById(link.getTargetEntityId());

        return Mono.zip(sourceSectionMono, targetPersonMono)
                .map(tuple -> {
                    Person targetPerson = tuple.getT2();
                    return new LinkPreviewDto(
                            tuple.getT1().getSectionType(),
                            link.getAnchorText(),
                            link.getTargetEntityType().name(),
                            null,
                            link.getTargetEntityId(),
                            targetPerson.getFullName(),
                            targetPerson.getShortBio(),
                            "/api/persons/by-date/" + targetPerson.getId()
                    );
                })
                .defaultIfEmpty(new LinkPreviewDto(
                        null, link.getAnchorText(), EntityType.PERSON.name(), null, link.getTargetEntityId(),
                        "Unknown Person", "Person not found.", null
                ));
    }



    private Mono<LinkPreviewDto> createLogSectionIncomingPreview(LogEntityLink link) {
        Mono<LogSection> sourceSectionMono = logSectionRepository.findById(Long.parseLong(link.getSourceEntityId()));
        Mono<DailyLog> sourceLogMono = sourceSectionMono.flatMap(s -> dailyLogRepository.findById(s.getDailyLogId()));

        return Mono.zip(sourceSectionMono, sourceLogMono)
                .map(tuple -> {
                    LogSection sourceSection = tuple.getT1();
                    DailyLog sourceLog = tuple.getT2();
                    return new LinkPreviewDto(
                            null,
                            link.getAnchorText(),
                            link.getSourceEntityType().name(),
                            sourceSection.getSectionType(),
                            link.getSourceEntityId(),
                            "From log on " + sourceLog.getLogDate(),
                            generateSnippet(sourceSection.getSummary(), link.getAnchorText()),
                            "/api/logbook/by-date/" + sourceLog.getLogDate()
                    );
                });
    }

    private Mono<String> bestTargetSnippetForDailyLog(Long targetLogId, String anchorText) {
        return logSectionRepository.findByDailyLogId(targetLogId)
                .collectList()
                .map(sections -> {
                    if (sections.isEmpty()) return anchorText;
                    for (LogSection s : sections) {
                        if (s.getSummary() != null && s.getSummary().contains(anchorText)) {
                            return generateSnippet(s.getSummary(), anchorText);
                        }
                    }
                    for (LogSection s : sections) {
                        if (s.getSummary() != null && !s.getSummary().isBlank()) {
                            return createSummaryPreview(s.getSummary());
                        }
                    }
                    return anchorText;
                });
    }
}