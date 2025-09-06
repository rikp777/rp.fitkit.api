package rp.fitkit.api.model.exercise;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("exercise_muscle_group")
public class ExerciseMuscleGroup {
    @Column("exercise_id")
    private String exerciseId;

    @Column("muscle_group_id")
    private String muscleGroupId;
}

