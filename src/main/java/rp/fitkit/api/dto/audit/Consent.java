package rp.fitkit.api.dto.audit;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Table("support_consent")
@Data
@NoArgsConstructor
public class Consent {

    @Id
    private UUID id;
    private String userId;
    private String justification;
    @CreatedDate
    private Instant grantedAt;
    private Instant expiresAt;
    private Instant revokedAt;

    public Consent(String userId, String justification, Instant expiresAt) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.justification = justification;
        this.expiresAt = expiresAt;
    }

    public boolean isValid() {
        return revokedAt == null && Instant.now().isBefore(expiresAt);
    }
}

