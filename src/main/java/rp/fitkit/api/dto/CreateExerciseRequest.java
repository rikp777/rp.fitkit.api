package rp.fitkit.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class CreateExerciseRequest {
    private double metValue;
    private String code;
    private List<String> muscleGroupCodes;
    private List<TranslationRequest> translations;
}
