package rp.fitkit.api.repository.user;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import rp.fitkit.api.model.user.UserSettings;

@Repository
public interface UserSettingsRepository extends ReactiveCrudRepository<UserSettings, String> { }

