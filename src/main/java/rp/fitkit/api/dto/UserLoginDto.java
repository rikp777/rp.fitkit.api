package rp.fitkit.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginDto {

    @Schema(description = "De gebruikersnaam van de gebruiker.", example = "test", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Username is required")
    private String username;

    @Schema(description = "Het wachtwoord van de gebruiker.", example = "password", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Password is required")
    private String password;
}
