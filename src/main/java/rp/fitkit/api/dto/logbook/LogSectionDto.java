package rp.fitkit.api.dto.logbook;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import rp.fitkit.api.model.root.SectionType;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogSectionDto {
    @Schema(description = "Het type sectie, bijv. MORNING of EVENING.", example = "EVENING")
    private SectionType sectionType;

    @Schema(description = "De inhoud van de logsectie.", example = "De avond was rustig. Teruggedacht aan de [ochtend](log:1) en hoe de dag verliep.")
    private String summary;

    @Schema(description = "De stemming die bij deze sectie hoort.", example = "Voldaan")
    private String mood;
}

