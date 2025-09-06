package rp.fitkit.api.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import rp.fitkit.api.model.exercise.Exercise;

@Repository
public interface ExerciseRepository extends R2dbcRepository<Exercise, String> {
}
