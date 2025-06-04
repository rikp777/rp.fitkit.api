package rp.fitkit.api.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString

@Table("set_log")
public class SetLog {
    private String id = UUID.randomUUID().toString();

    @Column("exercise_session_id")
    private String exerciseSessionId;

    private int reps;
    private double weight;

    public SetLog(String exerciseSessionId, int reps, double weight) {
        this.id = UUID.randomUUID().toString();
        this.exerciseSessionId = exerciseSessionId;
        this.reps = reps;
        this.weight = weight;
    }
}
