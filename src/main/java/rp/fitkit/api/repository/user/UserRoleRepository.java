package rp.fitkit.api.repository.user;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import rp.fitkit.api.model.user.UserRole;

@Repository
public interface UserRoleRepository extends R2dbcRepository<UserRole, String> {
    Flux<UserRole> findByUserId(String userId);
}

