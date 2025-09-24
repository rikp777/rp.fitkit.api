package rp.fitkit.api.model.user;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Data
@Table("password_recovery_codes")
public class PasswordRecoveryCode {

    @Id
    private UUID id = UUID.randomUUID();

    private UUID userId;

    private String codeHash;

    private boolean isUsed = false;
}
