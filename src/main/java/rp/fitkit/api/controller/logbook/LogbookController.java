package rp.fitkit.api.controller.logbook;

import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import rp.fitkit.api.dto.logbook.*;
import rp.fitkit.api.exception.InvalidDateRangeException;
import rp.fitkit.api.model.audit.AuditAction;
import rp.fitkit.api.model.logbook.LogSection;
import rp.fitkit.api.model.root.DailyLog;
import rp.fitkit.api.model.root.SectionType;
import rp.fitkit.api.model.user.User;
import rp.fitkit.api.service.audit.AuditService;
import rp.fitkit.api.service.logbook.LogbookService;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@RestController
@RequestMapping("/api/v1/logbook")
@RequiredArgsConstructor
public class LogbookController implements LogbookApi {

    private final LogbookService logbookService;
    private final AuditService auditService;

    @Override
    @GetMapping
    public Mono<Page<LogbookPreviewDto>> getLogbookHistory(
            @AuthenticationPrincipal
            User user,

            @RequestHeader(name = "Accept-Language", defaultValue = "en-GB")
            String languageCode,

            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,

            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size,

            @RequestParam(defaultValue = "logDate,desc")
            String[] sort
    ) {
        LocalDate end = (endDate == null) ? LocalDate.now() : endDate;
        LocalDate start = (startDate == null) ? end.minusDays(6) : startDate;

        if (ChronoUnit.DAYS.between(start, end) > 6) {
            return Mono.error(new InvalidDateRangeException("The date range cannot be larger than 7 days."));
        }

        String sortField = sort[0];
        String sortDirection = sort.length > 1 ? sort[1] : "desc";
        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));

        return logbookService.getPaginatedLogbooksForUser(user.getId(), start, end, pageable)
                .flatMap(pageResult -> auditService.logUserAction(
                        AuditAction.VIEW,
                        "LogbookHistory",
                        user.getId(),
                        pageResult
                ));
    }

    @Override
    @GetMapping("/{date}")
    public Mono<FullLogbookDto> getLogByDate(
            @AuthenticationPrincipal
            User user,

            @RequestHeader(name = "Accept-Language", defaultValue = "en-GB")
            String languageCode,

            @PathVariable("date")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date
    ) {
        return logbookService.getFullLogbook(user.getId(), date)
                .flatMap(logbookDto -> auditService.logUserAction(
                        AuditAction.VIEW,
                        DailyLog.class.getSimpleName(),
                        logbookDto.getLogId().toString(),
                        logbookDto
                ));
    }

    @Override
    @GetMapping("/today")
    public Mono<FullLogbookDto> getTodaysLog(
            @AuthenticationPrincipal
            User user,

            @RequestHeader(name = "Accept-Language", defaultValue = "en-GB")
            String languageCode
    ) {
        return logbookService.getFullLogbook(user.getId(), LocalDate.now())
                .flatMap(logbookDto -> auditService.logUserAction(
                        AuditAction.VIEW,
                        DailyLog.class.getSimpleName(),
                        logbookDto.getLogId().toString(),
                        logbookDto
                ));
    }

    @Override
    @PutMapping("/{date}/{sectionType}")
    public Mono<LogSectionDto> saveOrUpdateSection(
            @AuthenticationPrincipal
            User user,

            @RequestHeader(name = "Accept-Language", defaultValue = "en-GB")
            String languageCode,

            @PathVariable("date")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date,

            @PathVariable("sectionType")
            SectionType sectionType,

            @RequestBody
            SaveLogSectionRequest request
    ) {
        return logbookService.findOrCreateDailyLog(user.getId(), date)
                .flatMap(dailyLog -> logbookService.saveLogSection(
                        dailyLog.getId(),
                        sectionType,
                        request.getSummary(),
                        request.getMood()
                ))
                .flatMap(savedSection -> auditService.logUserAction(
                        AuditAction.UPDATE,
                        LogSection.class.getSimpleName(),
                        savedSection.getId().toString(),
                        savedSection
                ))
                .map(this::toLogSectionDto);
    }

    @Override
    @GetMapping("/graph-data/keywords")
    public Mono<GraphDataDto> getKeywordGraphData(
            @AuthenticationPrincipal User user,
            @RequestHeader(name = "Accept-Language", defaultValue = "en-GB") String languageCode
    ) {
        return logbookService.getKeywordGraphData(user.getId());
    }

    private LogSectionDto toLogSectionDto(LogSection section) {
        return new LogSectionDto(section.getSectionType(), section.getSummary(), section.getMood());
    }
}

