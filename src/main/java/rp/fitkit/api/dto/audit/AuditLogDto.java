package rp.fitkit.api.dto.audit;

import lombok.Data;
import rp.fitkit.api.model.audit.AuditAction;

import java.time.Instant;

@Data
public class AuditLogDto {
    private String actorUsername;
    private String actorRoles;
    private AuditAction action;
    private String subjectUsername;
    private String entityType;
    private String entityId;
    private String justification;
    private Instant timestamp;
}

