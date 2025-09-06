package rp.fitkit.api.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import rp.fitkit.api.model.exercise.ExerciseTranslation;

@Repository
public interface ExerciseTranslationRepository extends R2dbcRepository<ExerciseTranslation, String> {
    Flux<ExerciseTranslation> findByExerciseId(String exerciseId);
    Mono<ExerciseTranslation> findByExerciseIdAndLanguageCode(String exerciseId, String languageCode);
    Mono<Void> deleteByExerciseId(String exerciseId);
}
