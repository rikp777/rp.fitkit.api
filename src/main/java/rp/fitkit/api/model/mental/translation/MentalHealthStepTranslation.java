package rp.fitkit.api.model.mental.translation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("mental_health_step_translation")
public class MentalHealthStepTranslation {

    @Column("mental_health_step_id")
    private Long mentalHealthStepId;

    @Column("language_code")
    private String languageCode;

    private String title;

    private String description;

    private String purpose;
}

