package rp.fitkit.api.model.mental;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@Table("performed_action")
public class PerformedAction {
    @Id
    private Long id;

    private String userId;

    private Long mentalHealthStepId;

    private LocalDateTime performedAt;
}
