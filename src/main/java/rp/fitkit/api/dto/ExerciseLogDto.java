package rp.fitkit.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import rp.fitkit.api.model.SetLog;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExerciseLogDto {
    @Schema(description = "Naam van de gelogde oefening.", example = "Bench Press")
    @NotBlank(message = "exerciseName mag niet leeg zijn.")
    private String exerciseName;

    @Schema(description = "Datum van de workout. (Optioneel, standaard vandaag)", example = "2025-06-11")
    private LocalDate date;

    @Schema(description = "Lijst van sets die zijn uitgevoerd voor deze oefening.")
    @NotNull(message = "De lijst van sets mag niet null zijn.")
    @NotEmpty(message = "Je moet minimaal één set loggen.")
    @Valid
    private List<SetLogDto> sets;

    @Schema(description = "Optionele notities over de workout.", example = "Energie voelde goed vandaag.")
    private String notes;
}
