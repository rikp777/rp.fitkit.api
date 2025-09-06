package rp.fitkit.api.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import rp.fitkit.api.model.muscleGroup.MuscleGroupTranslation;

@Repository
public interface MuscleGroupTranslationRepository extends R2dbcRepository<MuscleGroupTranslation, String> {
    Mono<MuscleGroupTranslation> findByMuscleGroupIdAndLanguageCode(String muscleGroupId, String languageCode);
}
