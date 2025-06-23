package rp.fitkit.api.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@Table("consumption_log")
public class ConsumptionLog implements Persistable<String> {

    @Id
    private String id;

    @Column("user_id")
    private String userId;

    @Column("consumed_at")
    private LocalDateTime consumedAt;

    private String notes;

    @Transient
    private boolean isNew;

    public ConsumptionLog(String userId, LocalDateTime consumedAt, String notes) {
        this.id = UUID.randomUUID().toString();
        this.userId = userId;
        this.consumedAt = consumedAt;
        this.notes = notes;
        this.isNew = true;
    }

    @Override
    @Transient
    public boolean isNew() {
        return this.isNew || id == null;
    }

    public ConsumptionLog markAsNew() {
        this.isNew = true;
        return this;
    }
}
