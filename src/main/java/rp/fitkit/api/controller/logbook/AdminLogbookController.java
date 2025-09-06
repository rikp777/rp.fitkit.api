package rp.fitkit.api.controller.logbook;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import rp.fitkit.api.model.root.DailyLog;
import rp.fitkit.api.service.logbook.AdminLogbookService;

@RestController
@RequestMapping("/api/v1/admin/logbooks")
@RequiredArgsConstructor
@Tag(name = "Logbook (Admin)", description = "Admin-only endpoints for managing user logbooks.")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class AdminLogbookController {

    private final AdminLogbookService adminLogbookService;

    @GetMapping
    public Mono<Page<DailyLog>> getAllDailyLogs(Pageable pageable) {
        return adminLogbookService.getAllDailyLogs(pageable);
    }

    @GetMapping("/search")
    public Mono<Page<DailyLog>> searchDailyLogsByUsername(
            @RequestParam String username,
            Pageable pageable) {
        return adminLogbookService.searchDailyLogsByUsername(username, pageable);
    }

    @DeleteMapping("/{logId}")
    public Mono<ResponseEntity<Void>> deleteDailyLog(@PathVariable Long logId) {
        return adminLogbookService.deleteDailyLog(logId)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }
}
