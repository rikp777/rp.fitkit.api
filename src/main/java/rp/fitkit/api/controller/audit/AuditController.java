package rp.fitkit.api.controller.audit;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import rp.fitkit.api.dto.audit.AuditLogDto;
import rp.fitkit.api.model.audit.AuditLog;
import rp.fitkit.api.model.user.User;
import rp.fitkit.api.repository.audit.AuditLogRepository;

@RestController
@RequestMapping("/api/v1/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditLogRepository auditLogRepository;

    @GetMapping("/me")
    public Mono<Page<AuditLogDto>> getMyAuditHistory(
            @AuthenticationPrincipal User user,
            Pageable pageable) {
        return auditLogRepository.countBySubjectId(user.getId())
                .flatMap(total -> auditLogRepository.findBySubjectId(user.getId(), pageable)
                        .map(this::toDto)
                        .collectList()
                        .map(list -> new PageImpl<>(list, pageable, total))
                );
    }

    private AuditLogDto toDto(AuditLog auditLog) {
        AuditLogDto dto = new AuditLogDto();
        dto.setActorUsername(auditLog.getActorUsername());
        dto.setActorRoles(auditLog.getActorRoles());
        dto.setAction(auditLog.getAction());
        dto.setSubjectUsername(auditLog.getSubjectUsername());
        dto.setEntityType(auditLog.getEntityType());
        dto.setEntityId(auditLog.getEntityId());
        dto.setJustification(auditLog.getJustification());
        dto.setTimestamp(auditLog.getTimestamp());
        return dto;
    }
}

