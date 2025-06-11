package rp.fitkit.api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class WorkoutTemplateDto {
    private String id;
    private String name;
    private Integer dayOfWeek;
    private List<ExerciseTemplateDto> exercises;
}
