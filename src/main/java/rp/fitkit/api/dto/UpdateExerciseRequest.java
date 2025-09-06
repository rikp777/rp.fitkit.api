package rp.fitkit.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class UpdateExerciseRequest {
    private double metValue;
    private List<String> muscleGroupCodes;
    private List<TranslationRequest> translations;
}