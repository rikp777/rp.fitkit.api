package rp.fitkit.api.dto.logbook;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SaveLogSectionRequest {
    @NotNull
    private String summary;
    private String mood;
}

