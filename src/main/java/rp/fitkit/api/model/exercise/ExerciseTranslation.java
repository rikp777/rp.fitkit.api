package rp.fitkit.api.model.exercise;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("exercise_translation")
public class ExerciseTranslation {
    @Column("exercise_id")
    private String exerciseId;

    @Column("language_code")
    private String languageCode;

    private String name;
    private String description;
    private String instructions;
}
