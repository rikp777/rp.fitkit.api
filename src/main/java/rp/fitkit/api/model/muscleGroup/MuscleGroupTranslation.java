package rp.fitkit.api.model.muscleGroup;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("muscle_group_translation")
public class MuscleGroupTranslation {
    @Column("muscle_group_id")
    private String muscleGroupId;

    @Column("language_code")
    private String languageCode;

    private String name;
    private String description;
}

