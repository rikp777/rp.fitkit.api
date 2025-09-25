package rp.fitkit.api.model.user;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Data
@Table("password_recovery_codes")
public class PasswordRecoveryCode implements Persistable<UUID> {

    @Id
    private UUID id = UUID.randomUUID();

    private UUID userId;

    private String codeHash;

    private boolean isUsed = false;

    @Transient
    private boolean isNew = true;

    @Override
    public UUID getId() {
        return this.id;
    }

    @Override
    @Transient
    public boolean isNew() {
        return this.isNew;
    }

    public PasswordRecoveryCode() {
        this.id = UUID.randomUUID();
    }
}
