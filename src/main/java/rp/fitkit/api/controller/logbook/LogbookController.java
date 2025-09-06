package rp.fitkit.api.controller.logbook;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import rp.fitkit.api.dto.logbook.FullLogbookDto;
import rp.fitkit.api.dto.logbook.LogSectionDto;
import rp.fitkit.api.dto.logbook.LogbookPreviewDto;
import rp.fitkit.api.dto.logbook.SaveLogSectionRequest;
import rp.fitkit.api.model.logbook.LogSection;
import rp.fitkit.api.model.root.DailyLog;
import rp.fitkit.api.model.root.SectionType;
import rp.fitkit.api.model.user.User;
import rp.fitkit.api.service.logbook.LogbookService;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@RestController
@RequestMapping("/api/v1/logbook")
@RequiredArgsConstructor
@Tag(name = "Logbook", description = "Endpoints for managing the user's daily logbook.")
@SecurityRequirement(name = "bearerAuth")
public class LogbookController {

    private final LogbookService logbookService;

    @GetMapping
    public Mono<Page<LogbookPreviewDto>> getLogbookHistory(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "logDate,desc") String[] sort) {

        LocalDate end = (endDate == null) ? LocalDate.now() : endDate;
        LocalDate start = (startDate == null) ? end.minusDays(6) : startDate;

        if (ChronoUnit.DAYS.between(start, end) > 6) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "The date range cannot be larger than 7 days."));
        }
        //todo premium feature - extend range

        String sortField = sort[0];
        String sortDirection = sort.length > 1 ? sort[1] : "desc";
        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));

        return logbookService.getPaginatedLogbooksForUser(user.getId(), start, end, pageable);
    }

    @GetMapping("/by-date")
    public Mono<FullLogbookDto> getLogByDate(
            @AuthenticationPrincipal User user,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return logbookService.getFullLogbook(user.getId(), date);
    }

    @GetMapping("/today")
    public Mono<FullLogbookDto> getTodaysLog(@AuthenticationPrincipal User user) {
        return logbookService.getFullLogbook(user.getId(), LocalDate.now());
    }

    @PostMapping("/today/{sectionType}")
    public Mono<LogSectionDto> saveSection(
            @AuthenticationPrincipal User user,
            @PathVariable SectionType sectionType,
            @Valid @RequestBody SaveLogSectionRequest request) {

        LocalDate today = LocalDate.now();
        return logbookService.findOrCreateDailyLog(user.getId(), today)
                .flatMap(dailyLog -> logbookService.saveLogSection(
                        dailyLog.getId(),
                        sectionType,
                        request.getSummary(),
                        request.getMood()
                ))
                .map(this::toLogSectionDto);
    }

    private LogSectionDto toLogSectionDto(LogSection section) {
        return new LogSectionDto(section.getSectionType(), section.getSummary(), section.getMood());
    }
}

