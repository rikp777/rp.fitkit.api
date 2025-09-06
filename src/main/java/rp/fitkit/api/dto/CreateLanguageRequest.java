package rp.fitkit.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreateLanguageRequest {
    private String code;
    private String name;
}
