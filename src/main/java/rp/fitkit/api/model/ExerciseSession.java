package rp.fitkit.api.model;

import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"sets"})
public class ExerciseSession {
    private String id;
    private String userId;
    private String exerciseName;
    private LocalDate date;
    private List<SetLog> sets;
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
