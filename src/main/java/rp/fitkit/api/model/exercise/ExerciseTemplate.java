package rp.fitkit.api.model.exercise;

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
@Table("exercise_template")
public class ExerciseTemplate implements Persistable<UUID> {

    @Id
    private UUID id;

    @Column("workout_template_id")
    private UUID workoutTemplateId;

    @Column("exercise_id")
    private String exerciseId;

    @Column("target_sets")
    private Integer targetSets;

    @Column("target_reps_min")
    private Integer targetRepsMin;

    @Column("target_reps_max")
    private Integer targetRepsMax;

    @Column("target_rpe_min")
    private Integer targetRpeMin;

    @Column("target_rpe_max")
    private Integer targetRpeMax;

    @Column("rest_period_seconds")
    private Integer restPeriodSeconds;

    @Column("display_order")
    private int displayOrder;

    @Transient
    private boolean isNew;

    public ExerciseTemplate(UUID workoutTemplateId, String exerciseId, Integer targetSets, Integer targetRepsMin, Integer targetRepsMax, Integer targetRpeMin, Integer targetRpeMax, Integer restPeriodSeconds, int displayOrder) {
        this.id = UUID.randomUUID();
        this.workoutTemplateId = workoutTemplateId;
        this.exerciseId = exerciseId;
        this.targetSets = targetSets;
        this.targetRepsMin = targetRepsMin;
        this.targetRepsMax = targetRepsMax;
        this.targetRpeMin = targetRpeMin;
        this.targetRpeMax = targetRpeMax;
        this.restPeriodSeconds = restPeriodSeconds;
        this.displayOrder = displayOrder;
        this.isNew = true;
    }

    @Override
    @Transient
    public boolean isNew() {
        return this.isNew || id == null;
    }

    public ExerciseTemplate MarkAsNew() {
        this.isNew = true;
        return this;
    }
}
