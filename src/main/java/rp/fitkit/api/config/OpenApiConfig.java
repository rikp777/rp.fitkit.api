package rp.fitkit.api.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.*;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "FitKit API",
                version = "1.0.0",
                description = "API voor de FitKit fitness- en maaltijdapp."
        ),
        security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.OAUTH2,
        flows = @OAuthFlows(
                password = @OAuthFlow(
                        tokenUrl = "/api/v1/auth/login",
                        scopes = {}
                )
        )
)
public class OpenApiConfig {

    @Bean
    public GroupedOpenApi logbookApi() {
        return GroupedOpenApi.builder()
                .group("logbook")
                .pathsToMatch(
                        "/api/v1/logbook/**",
                        "/api/v1/admin/logbook/**",
                        "/api/v1/audit/**",
                        "/api/v1/consent/**"
                )
                .build();
    }

    @Bean
    public GroupedOpenApi userApi() {
        return GroupedOpenApi.builder()
                .group("user-management")
                .pathsToMatch("/api/v1/users/**", "/api/v1/auth/**")
                .build();
    }

    @Bean
    public GroupedOpenApi workoutApi() {
        return GroupedOpenApi.builder()
                .group("workouts")
                .pathsToMatch("/api/v1/workouts/**", "/api/v1/exercises/**")
                .build();
    }
}
