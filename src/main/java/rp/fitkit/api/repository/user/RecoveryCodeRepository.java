package rp.fitkit.api.repository.user;


import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import rp.fitkit.api.model.user.PasswordRecoveryCode;

import java.util.UUID;

@Repository
public interface RecoveryCodeRepository extends R2dbcRepository<PasswordRecoveryCode, UUID> {

    /**
     * Finds all recovery codes for a given user that match the 'isUsed' status.
     * Spring Data R2DBC will automatically generate the query for this method.
     *
     * @param userId The ID of the user.
     * @param isUsed The usage status to filter by (typically 'false').
     * @return A Flux emitting the found recovery codes.
     */
    Flux<PasswordRecoveryCode> findByUserIdAndIsUsed(UUID userId, boolean isUsed);

    /**
     * Deletes all recovery codes associated with a specific user.
     * This is used to clear old codes before generating new ones.
     *
     * @param userId The ID of the user whose codes should be deleted.
     * @return A Mono that completes when the deletion is done.
     */
    Mono<Void> deleteAllByUserId(UUID userId);
}
