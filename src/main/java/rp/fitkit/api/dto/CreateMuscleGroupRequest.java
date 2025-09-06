package rp.fitkit.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateMuscleGroupRequest {
    private String code;
    private String latinName;
    private List<TranslationRequest> translations;
}
