package rp.fitkit.api.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Data
@Table("exercise_template")
public class ExerciseTemplate implements Persistable<String> {
    @Id
    private String id = UUID.randomUUID().toString();
    @Column("workout_template_id")
    private String workoutTemplateId;
    @Column("exercise_name")
    private String exerciseName;
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
    @Column("\"order\"")
    private int order;

    @Transient
    private boolean isNew = true;

    @Override
    @Transient
    public boolean isNew() {
        return this.isNew;
    }
}
