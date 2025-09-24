package rp.fitkit.api.repository.user;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import rp.fitkit.api.model.user.UserSettings;

import java.util.UUID;

@Repository
public interface UserSettingsRepository extends ReactiveCrudRepository<UserSettings, UUID> { }

