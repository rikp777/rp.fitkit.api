package rp.fitkit.api.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class GenerateRecoveryCodesResponseDto {
    private List<String> recoveryCodes;
    private String message;
}
