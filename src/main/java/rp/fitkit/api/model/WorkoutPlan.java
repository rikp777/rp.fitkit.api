package rp.fitkit.api.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Data
@NoArgsConstructor
@Table("workout_plan")
public class WorkoutPlan implements Persistable<UUID> {

    @Id
    private UUID id;

    @Column("user_id")
    private UUID userId;

    private String name;
    private String description;

    @Column("is_active")
    private boolean isActive;

    @Transient
    private boolean isNew;

    public WorkoutPlan(UUID userId, String name, String description, boolean isActive) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.name = name;
        this.description = description;
        this.isActive = isActive;
        this.isNew = true;
    }

    @Override
    @Transient
    public boolean isNew() {
        return this.isNew || id == null;
    }

    public WorkoutPlan markAsNew() {
        this.isNew = true;
        return this;
    }
}
