package rp.fitkit.api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
public class WorkoutPlanDto {
    private UUID id;
    private UUID userId;
    private String name;
    private String description;
    private boolean isActive;
    private List<WorkoutTemplateDto> workoutTemplates;
}

