package rp.fitkit.api.dto.logbook;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogbookPreviewDto {
    @Schema(description = "De unieke ID van het dagelijkse logboek.", example = "1")
    private Long logId;

    @Schema(description = "De datum van dit logboek.", example = "2025-09-03")
    private LocalDate logDate;

    @Schema(description = "Een kort fragment van de inhoud van die dag.", example = "De dag begon goed, veel energie. Ik heb nagedacht over het nieuwe project...")
    private String summaryPreview;

    @Schema(description = "Een lijst van de stemmingen die op deze dag zijn vastgelegd.", example = "[\"Energiek\", \"Voldaan\"]")
    private List<String> moods;
}

