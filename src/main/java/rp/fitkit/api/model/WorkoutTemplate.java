package rp.fitkit.api.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Data
@Table("workout_template")
public class WorkoutTemplate implements Persistable<String> {
    @Id
    private String id = UUID.randomUUID().toString();
    @Column("workout_plan_id")
    private String workoutPlanId;
    private String name;
    @Column("day_of_week")
    private Integer dayOfWeek;

    @Transient
    private boolean isNew = true;

    @Override
    @Transient
    public boolean isNew() {
        return this.isNew;
    }
}
