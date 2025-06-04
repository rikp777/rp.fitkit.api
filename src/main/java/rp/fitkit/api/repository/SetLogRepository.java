package rp.fitkit.api.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import rp.fitkit.api.model.SetLog;

public interface SetLogRepository extends R2dbcRepository<SetLog, String> {
    Flux<SetLog> findByExerciseSessionId(String exerciseSessionId);
}
