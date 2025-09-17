package rp.fitkit.api.controller.audit;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.constraints.Max;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import rp.fitkit.api.dto.audit.AuditLogDto;
import rp.fitkit.api.exception.InvalidSortFieldException;
import rp.fitkit.api.model.audit.AuditLog;
import rp.fitkit.api.model.user.User;
import rp.fitkit.api.repository.audit.AuditLogRepository;
import rp.fitkit.api.service.audit.AuditService;

import java.util.Set;

@RestController
@RequestMapping("/api/v1/audit")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Slf4j
public class AuditController {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "actorUsername",
            "actorRoles",
            "action",
            "subjectUsername",
            "entityType",
            "entityId",
            "justification",
            "timestamp"
    );

    private final AuditService auditService;

    @GetMapping("/me")
    public Mono<Page<AuditLogDto>> getMyAuditHistory(
            @AuthenticationPrincipal
            User user,

            @RequestParam(defaultValue = "0")
            int page,

            @Parameter(
                    description = "Aantal resultaten per pagina.",
                    schema = @Schema(
                            type = "integer",
                            defaultValue = "50",
                            allowableValues = {"50", "100", "250", "500"}
                    )
            )
            @RequestParam(defaultValue = "50") @Max(500)
            int size,

            @RequestParam(defaultValue = "timestamp,desc")
            String[] sort
    ) {
        log.debug("Request getMyAuditHistory for user: {}, page: {}, size: {}, sort: {}",
                user.getId(), page, size, String.join(",", sort));

        Pageable pageable = createPageable(page, size, sort);

        return auditService.getAuditHistoryForUser(user, pageable)
                .doOnSuccess(resultPage -> log.debug(
                        "Successfully returned page {}/{} for user {}",
                        resultPage.getNumber(), resultPage.getTotalPages(), user.getId()
                ));
    }

    private Pageable createPageable(int page, int size, String[] sort) {
        String sortField = sort[0];
        if (!ALLOWED_SORT_FIELDS.contains(sortField)) {
            log.warn("Invalid sort field '{}' requested", sortField);
            throw new InvalidSortFieldException(sortField, ALLOWED_SORT_FIELDS);
        }
        String sortDirection = sort.length > 1 ? sort[1] : "desc";
        return PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDirection), sortField));
    }
}

