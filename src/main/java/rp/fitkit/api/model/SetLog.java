package rp.fitkit.api.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString

@Table("set_log")
public class SetLog implements Persistable<String> {
    @Id
    private String id;

    @Column("exercise_session_id")
    private String exerciseSessionId;

    private int reps;
    private double weight;
    private int rpe;

    @Transient
    private boolean isNew;

    public SetLog(String exerciseSessionId, int reps, double weight, int rpe) {
        this.id = UUID.randomUUID().toString();
        this.isNew = true;
        this.exerciseSessionId = exerciseSessionId;
        this.reps = reps;
        this.weight = weight;
        this.rpe = rpe;
    }

    @Override
    @Transient
    public boolean isNew() {
        return this.isNew;
    }
}
