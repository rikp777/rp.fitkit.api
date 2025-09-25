package rp.fitkit.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import rp.fitkit.api.logging.RateLimitingFilter;

import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;


@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {

    private final ReactiveAuthenticationManager authenticationManager;
    private final ServerSecurityContextRepository securityContextRepository;
    private final RateLimitingFilter rateLimitingFilter;

    public SecurityConfig(
            ReactiveAuthenticationManager authenticationManager,
            ServerSecurityContextRepository securityContextRepository,
            RateLimitingFilter rateLimitingFilter
    ) {
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
        this.rateLimitingFilter = rateLimitingFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(cors -> {}) // ✅ Enable CORS
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .authenticationManager(authenticationManager)
                .securityContextRepository(securityContextRepository)
                .authorizeExchange(exchange -> exchange
                        .pathMatchers(
                                "/",
                                "/auth/**",
                                "/dashboard",
                                "/src/**", "/css/**",
                                "/mental-health/**"
                        ).permitAll()
                        .pathMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/v3/api-docs.yaml",
                                "/v3/api-docs/swagger-config"
                        ).permitAll()

                        .pathMatchers(HttpMethod.POST, "/api/v1/auth/register", "/api/v1/auth/login", "/api/v1/auth/refresh").permitAll()
                        .pathMatchers("/api/v1/admin/**").hasRole("ADMIN")
                        .pathMatchers("/actuator/**").hasRole("ADMIN")
                        .pathMatchers("/actuator/health", "/actuator/info", "/actuator/prometheus").permitAll()
                        .anyExchange().authenticated()
                )
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((exchange, ex) -> Mono.fromRunnable(() -> {
                            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                        }))
                        .accessDeniedHandler((exchange, denied) -> Mono.fromRunnable(() -> {
                            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                        }))
                )
                .addFilterAt(rateLimitingFilter, SecurityWebFiltersOrder.FIRST)
                .build();
    }

    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.setAllowCredentials(true);
        corsConfig.addAllowedOrigin("http://localhost:5173");
        corsConfig.addAllowedOrigin("http://localhost:3000");
        corsConfig.addAllowedHeader("*");
        corsConfig.addAllowedMethod("*");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        return source;
    }

    @Bean
    public RouterFunction<ServerResponse> htmlRouter() {
        return RouterFunctions
                .route(GET("/"), request ->
                        ok().bodyValue(new ClassPathResource("static/app/pages/index.html")))

                .andRoute(GET("/auth/login"), request ->
                        ok().bodyValue(new ClassPathResource("static/app/pages/auth/login.html")))

                .andRoute(GET("/auth/register"), request ->
                        ok().bodyValue(new ClassPathResource("static/app/pages/auth/register.html")))

                .andRoute(GET("/auth/reset"), request ->
                        ok().bodyValue(new ClassPathResource("static/app/pages/auth/reset-password.html")))

                .andRoute(GET("/dashboard"), request ->
                        ok().bodyValue(new ClassPathResource("static/app/pages/dashboard.html")))

                // Mental Health
                .andRoute(GET("/mental-health"), request ->
                        ok().bodyValue(new ClassPathResource("static/app/pages/mental/dashboard.html")))

                .andRoute(GET("/mental-health/dashboard/today"), request -> {
                    String todayDate = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
                    URI uri = URI.create("/mental-health/dashboard/" + todayDate);
                    return ServerResponse.temporaryRedirect(uri).build();
                })
                .andRoute(GET("/mental-health/dashboard"), request -> {
                    String todayDate = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
                    URI uri = URI.create("/mental-health/dashboard/" + todayDate);
                    return ServerResponse.temporaryRedirect(uri).build();
                })
                .andRoute(GET("/mental-health/dashboard/{date:[0-9]{4}-[0-9]{2}-[0-9]{2}}"), request ->
                        ok().bodyValue(new ClassPathResource("static/app/pages/mental/dashboard.html")));
    }
}