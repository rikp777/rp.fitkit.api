package rp.fitkit.api.model.mental;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("user_step_progress")
public class UserStepProgress {

    @Id
    private Long id;

    private String userId;

    private Long mentalHealthStepId;

    private int completionCount;
}

