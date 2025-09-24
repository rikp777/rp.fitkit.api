package rp.fitkit.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import rp.fitkit.api.model.SetLog;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExerciseSessionResponseDto {
    private UUID id;
    private String exerciseName;
    private LocalDate date;
    private String notes;
    private List<SetLog> sets;
}
