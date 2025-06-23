package rp.fitkit.api.dto;

import lombok.Data;

@Data
public class TranslationRequest {
    private String languageCode;
    private String name;
    private String description;
    private String instructions;
}
