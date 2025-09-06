package rp.fitkit.api.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import rp.fitkit.api.model.muscleGroup.MuscleGroup;

import java.util.List;

@Repository
public interface MuscleGroupRepository extends R2dbcRepository<MuscleGroup, String> {

    Mono<MuscleGroup> findByCode(String code);

    Flux<MuscleGroup> findByCodeIn(List<String> codes);

    @Query("SELECT * FROM muscle_group WHERE id = :id")
    Mono<MuscleGroup> findById(String id);
}
