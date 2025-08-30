package rp.fitkit.api.dto.mental.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MentalHealthStepTranslationRequest {
    @NotBlank
    @Size(max = 5)
    private String languageCode;

    @NotBlank
    @Size(max = 255)
    private String title;

    private String description;
    private String purpose;
}

