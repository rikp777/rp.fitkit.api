package rp.fitkit.api.repository.user;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;
import rp.fitkit.api.model.user.User;

public interface UserRepository extends R2dbcRepository<User, String> {
    Mono<User> findByUsername(String username);
    Mono<User> findByEmail(String email);
}