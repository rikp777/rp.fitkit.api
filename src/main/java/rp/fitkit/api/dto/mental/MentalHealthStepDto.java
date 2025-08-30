package rp.fitkit.api.dto.mental;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MentalHealthStepDto {
    private Long id;
    private int stepNumber;
    private String title;
    private String description;
    private String purpose;
    private boolean isUnlocked;
    private int requiredCompletions;
    private int userCompletions;
}

