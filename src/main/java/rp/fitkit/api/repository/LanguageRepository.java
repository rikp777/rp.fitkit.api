package rp.fitkit.api.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import rp.fitkit.api.model.Language;

@Repository
public interface LanguageRepository extends R2dbcRepository<Language, String> {
}
