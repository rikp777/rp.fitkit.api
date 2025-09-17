package rp.fitkit.api.service.user;

import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import rp.fitkit.api.repository.user.UserRepository;

@Service
public class CustomUserDetailsService implements ReactiveUserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return userRepository

                .findByUsername(username)
                .switchIfEmpty(Mono.defer(() -> userRepository.findByEmail(username)))

                .flatMap(user ->
                        userRepository.findRolesByUserId(user.getId())
                                .collectList()
                                .map(roles -> {
                                    user.setAuthoritiesFromRoles(roles);
                                    return user;
                                })
                )
                .cast(UserDetails.class)

                .switchIfEmpty(Mono.error(new UsernameNotFoundException("User not found: " + username)));
    }
}
