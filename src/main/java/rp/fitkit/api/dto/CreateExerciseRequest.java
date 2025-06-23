package rp.fitkit.api.dto;

import lombok.Data;

import java.util.List;

@Data
public class CreateExerciseRequest {
    private double metValue;
    private String primaryMuscleGroup;

    private List<TranslationRequest> translations;
}
