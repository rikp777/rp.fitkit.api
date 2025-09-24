package rp.fitkit.api.model;

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
@Table("workout_template")
public class WorkoutTemplate implements Persistable<UUID> {

    @Id
    private UUID id;

    @Column("workout_plan_id")
    private UUID workoutPlanId;

    private String name;

    @Column("day_of_week")
    private Integer dayOfWeek;

    @Transient
    private boolean isNew;

    public WorkoutTemplate(UUID workoutPlanId, String name, Integer dayOfWeek) {
        this.id = UUID.randomUUID();
        this.workoutPlanId = workoutPlanId;
        this.name = name;
        this.dayOfWeek = dayOfWeek;
        this.isNew = true;
    }

    @Override
    @Transient
    public boolean isNew() {
        return this.isNew || id == null;
    }

    public WorkoutTemplate markAsNew() {
        this.isNew = true;
        return this;
    }
}

