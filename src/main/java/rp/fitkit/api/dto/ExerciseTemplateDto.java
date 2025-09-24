package rp.fitkit.api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class ExerciseTemplateDto {
    private UUID id;
    private String exerciseName;
    private int order;
    private Integer targetSets;
    private Integer targetRepsMin;
    private Integer targetRepsMax;
    private Integer targetRpeMin;
    private Integer targetRpeMax;
    private Integer restPeriodSeconds;
}
