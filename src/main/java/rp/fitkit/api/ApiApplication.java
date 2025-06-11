package rp.fitkit.api;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.core.context.SecurityContextHolder;
import reactor.core.publisher.Hooks;

@SpringBootApplication
@OpenAPIDefinition
public class ApiApplication {

    public static void main(String[] args) {
        Hooks.onOperatorDebug(); // Enable operator debugging for Reactor //todo remove in production

        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
        SpringApplication.run(ApiApplication.class, args);
    }
}
