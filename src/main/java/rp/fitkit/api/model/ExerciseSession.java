package rp.fitkit.api.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"sets"})
@Table("exercise_session")
public class ExerciseSession {

    @Id
    private String id = UUID.randomUUID().toString();
    @Column("user_id")
    private String userId;
    @Column("exercise_name")
    private String exerciseName;
    @Column("session_date")
    private LocalDate date = LocalDate.now();

    @Transient
    private List<SetLog> sets = new ArrayList<>();
    private String notes;

    public ExerciseSession(String userId, String exerciseName) {
        this.userId = userId;
        this.exerciseName = exerciseName;
    }

    public ExerciseSession(String userId, String exerciseName, LocalDate date, List<SetLog> sets, String notes) {
        this.userId = userId;
        this.exerciseName = exerciseName;
        this.date = date;
        this.sets = (sets != null) ? new ArrayList<>(sets) : new ArrayList<>();
        this.notes = notes;
    }

    public void addSet(SetLog set) {
        this.sets.add(set);
    }
}
