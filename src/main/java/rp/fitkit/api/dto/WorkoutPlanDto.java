package rp.fitkit.api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class WorkoutPlanDto {
    private String id;
    private String userId;
    private String name;
    private String description;
    private boolean isActive;
    private List<WorkoutTemplateDto> workoutTemplates;
}

