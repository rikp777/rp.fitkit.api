package rp.fitkit.api.dto.audit;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConsentResponseDto {
    private UUID consentId;
    private String justification;
    private Instant expiresAt;
}

