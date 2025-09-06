package rp.fitkit.api.model.exercise;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import rp.fitkit.api.model.SetLog;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"sets"})
@Table("exercise_session")
public class ExerciseSession implements Persistable<String> {

    @Id
    private String id = UUID.randomUUID().toString();
    @Column("user_id")
    private String userId;
    @Column("exercise_id")
    private String exerciseId;
    @Column("session_date")
    private LocalDate date = LocalDate.now();
    @Column("duration_minutes")
    private Integer durationMinutes;

    @Transient
    private List<SetLog> sets = new ArrayList<>();
    private String notes;

    @Transient
    private boolean isNew;

    public ExerciseSession(String userId) {
        this.id = UUID.randomUUID().toString();
        this.date = LocalDate.now();
        this.isNew = true;
        this.userId = userId;
    }

    public ExerciseSession(String userId, LocalDate date, List<SetLog> sets, String notes) {
        this.userId = userId;
        this.date = date;
        this.sets = (sets != null) ? new ArrayList<>(sets) : new ArrayList<>();
        this.notes = notes;
    }

    public void addSet(SetLog set) {
        this.sets.add(set);
    }

    @Override
    @Transient
    public boolean isNew() {
        return this.isNew;
    }
}
