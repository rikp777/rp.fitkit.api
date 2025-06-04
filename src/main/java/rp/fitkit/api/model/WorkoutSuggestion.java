package rp.fitkit.api.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;
import lombok.Value;

import java.util.List;

@Value
public class WorkoutSuggestion {
    String exerciseName;
    List<SetLog> suggestedSets;
    String message;
}
