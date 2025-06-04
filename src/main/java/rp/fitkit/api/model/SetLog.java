package rp.fitkit.api.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data // Genereert getters, setters, toString, equals, hashCode
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SetLog {
    private int reps;
    private double weight;
}
