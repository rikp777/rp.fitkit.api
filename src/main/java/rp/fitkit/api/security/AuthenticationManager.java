package rp.fitkit.api.security;

import io.jsonwebtoken.Claims;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import rp.fitkit.api.service.user.CustomUserDetailsService;
import rp.fitkit.api.util.JwtUtil;


@Component
@AllArgsConstructor
@Slf4j
public class AuthenticationManager implements ReactiveAuthenticationManager {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        String authToken = authentication.getCredentials().toString();
        String username;

        try {
            username = jwtUtil.getUsernameFromToken(authToken);
        } catch (Exception e) {
            log.warn("Fout bij het parsen van JWT: {}", e.getMessage());
            return Mono.empty();
        }

        if (username != null && jwtUtil.validateToken(authToken)) {
            return userDetailsService.findByUsername(username)
                    .map(userDetails -> {
                        log.debug("Gebruiker '{}' succesvol geauthenticeerd via token.", userDetails.getUsername());
                        return new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );
                    })
                    .cast(Authentication.class)
                    .onErrorResume(UsernameNotFoundException.class, ex -> {
                        log.warn("Authenticatiepoging voor onbekende gebruiker: {}", username);
                        return Mono.empty();
                    });
        } else {
            return Mono.empty();
        }
    }
}