package rp.fitkit.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SetLogDto {

    @Schema(description = "Aantal uitgevoerde herhalingen.", example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
    @Min(value = 1, message = "Aantal herhalingen moet minimaal 1 zijn.")
    private int reps;

    @Schema(description = "Gewicht gebruikt voor de set (in kg).", example = "80.5", requiredMode = Schema.RequiredMode.REQUIRED)
    @Min(value = 0, message = "Gewicht kan niet negatief zijn.")
    private double weight;

    @Schema(description = "Rate of Perceived Exertion (1-10), hoe zwaar de set voelde.", example = "8")
    private int rpe;
}
