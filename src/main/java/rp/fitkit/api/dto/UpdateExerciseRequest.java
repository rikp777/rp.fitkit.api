package rp.fitkit.api.dto;

import lombok.Data;

import java.util.List;

@Data
public class UpdateExerciseRequest {
    private double metValue;
    private String primaryMuscleGroup;
    private List<TranslationRequest> translations;
}