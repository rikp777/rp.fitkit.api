package rp.fitkit.api.model.mental;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("mental_health_step")
public class MentalHealthStep {

    @Id
    private Long id;

    private int stepNumber;

    private int requiredCompletions;
}

