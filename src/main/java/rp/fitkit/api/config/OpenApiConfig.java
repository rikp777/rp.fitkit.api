package rp.fitkit.api.config;

import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "FitKit API",
                version = "1.0.0",
                description = "API voor de FitKit fitness- en maaltijdapp. Beheer je workouts, dieet en voortgang.",
                contact = @Contact(
                        name = "FitKit Support",
                        url = "https://www.fitkitapp.com/support",
                        email = "support@fitkitapp.com"
                )
        ),
        externalDocs = @ExternalDocumentation(
                description = "Meer informatie over FitKit",
                url = "https://docs.fitkitapp.com"
        ),
        security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer",
        description = "Voer hier je JWT Bearer token in, bijv. 'Bearer YOUR_TOKEN_HIER'"
)
public class OpenApiConfig {
}
