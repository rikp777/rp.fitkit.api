package rp.fitkit.api.dto;

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
    private String exerciseName;
    private LocalDate date;
    private List<SetLog> sets;
    private String notes;
}
