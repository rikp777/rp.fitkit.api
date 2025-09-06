package rp.fitkit.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExerciseDto {
    private String id;
    private String name;
    private String description;
    private String instructions;
    private double metValue;
    private List<MuscleGroupDto> muscleGroups;
}
