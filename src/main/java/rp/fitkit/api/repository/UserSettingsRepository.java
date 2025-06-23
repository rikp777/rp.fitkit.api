package rp.fitkit.api.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import rp.fitkit.api.model.UserSettings;

@Repository
public interface UserSettingsRepository extends ReactiveCrudRepository<UserSettings, String> { }

