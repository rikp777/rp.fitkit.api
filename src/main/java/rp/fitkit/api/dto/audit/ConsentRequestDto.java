package rp.fitkit.api.dto.audit;

import lombok.Data;

@Data
public class ConsentRequestDto {
    private String justification; // e.g., "TICKET-12345"
    private long durationHours = 2; // Default to 2 hours
}

