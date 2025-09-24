package rp.fitkit.api.model.audit;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import rp.fitkit.api.model.user.User;

import java.time.Instant;
import java.util.UUID;

@Table(name = "audit_log")
@Data
@NoArgsConstructor
public class AuditLog {

    @Id
    private Long id;

    // Who performed the action
    private UUID actorId;
    private String actorUsername;
    private String actorRoles;

    private AuditAction action;

    // Whose data was affected
    private UUID subjectId;
    private String subjectUsername;

    // What entity was affected
    private String entityType;
    private String entityId;

    private String justification;

    @CreatedDate
    private Instant timestamp;

    public AuditLog(User actor, AuditAction action, User subject, String entityType, String entityId, String justification) {
        this.actorId = actor.getId();
        this.actorUsername = actor.getUsername();
        this.actorRoles = actor.getAuthorities().stream()
                .map(grantedAuthority -> grantedAuthority.getAuthority().replace("ROLE_", ""))
                .reduce((a, b) -> a + "," + b)
                .orElse("");
        this.action = action;
        this.subjectId = subject.getId();
        this.subjectUsername = subject.getUsername();
        this.entityType = entityType;
        this.entityId = entityId;
        this.justification = justification;
    }
}


