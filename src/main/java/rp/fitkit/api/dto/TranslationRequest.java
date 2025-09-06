package rp.fitkit.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TranslationRequest {
    private String languageCode;
    private String name;
    private String description;
    private String instructions;
}
