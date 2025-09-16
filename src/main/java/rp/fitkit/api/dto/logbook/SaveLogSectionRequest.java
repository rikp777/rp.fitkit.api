package rp.fitkit.api.dto.logbook;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SaveLogSectionRequest {
    @NotBlank
    @Size(max = 5000)
    @Schema(
            description = "De inhoud van de logsectie. Markdown-achtige links worden ondersteund, bijv. [ankertekst](log:123).",
            example = "Vandaag het [project](log:2) afgerond. Voelt goed! Morgen focus op het nieuwe [initiatief](log:3)."
    )
    private String summary;

    @Size(max = 50)
    @Schema(
            description = "Een enkel woord dat de stemming beschrijft.",
            example = "Voldaan"
    )
    private String mood;
}

