package rp.fitkit.api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
public class WorkoutTemplateDto {
    private UUID id;
    private String name;
    private Integer dayOfWeek;
    private List<ExerciseTemplateDto> exercises;
}
