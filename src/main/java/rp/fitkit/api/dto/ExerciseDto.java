package rp.fitkit.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExerciseDto {
    private String id;
    private String name;
    private String description;
    private String instructions;
    private double metValue;
    private String primaryMuscleGroup;
}
