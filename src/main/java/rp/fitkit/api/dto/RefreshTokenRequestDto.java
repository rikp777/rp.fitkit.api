package rp.fitkit.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class RefreshTokenRequestDto {
    @JsonProperty("refresh_token")
    private String refreshToken;
}

