package rp.fitkit.api.dto.mental.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class CreateMentalHealthStepRequest {
    @Min(1)
    private int stepNumber;

    @Min(0)
    private int requiredCompletions;

    @NotEmpty
    @Valid
    private List<MentalHealthStepTranslationRequest> translations;
}

