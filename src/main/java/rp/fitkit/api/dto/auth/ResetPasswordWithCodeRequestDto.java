package rp.fitkit.api.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResetPasswordWithCodeRequestDto {
    @NotBlank
    private String username;
    @NotBlank
    private String recoveryCode;
    @NotBlank
    private String newPassword;
}
