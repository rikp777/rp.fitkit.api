package rp.fitkit.api.repository.user;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import rp.fitkit.api.model.user.UserRole;

import java.util.UUID;

@Repository
public interface UserRoleRepository extends R2dbcRepository<UserRole, UUID> {
    Flux<UserRole> findByUserId(UUID userId);
}

