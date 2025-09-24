package rp.fitkit.api.repository.user;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import rp.fitkit.api.model.user.User;

import java.util.List;
import java.util.UUID;

public interface UserRepository extends R2dbcRepository<User, UUID> {
    Mono<User> findByUsername(String username);
    Flux<User> findByUsernameContainingIgnoreCase(String username);

    Mono<User> findByEmail(String email);

    @Query("SELECT role_name FROM user_role WHERE user_id = :userId")
    Flux<String> findRolesByUserId(UUID userId);
}