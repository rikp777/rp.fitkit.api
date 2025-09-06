package rp.fitkit.api.model.mental;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@Table("performed_action")
@AllArgsConstructor
@NoArgsConstructor
public class PerformedAction {
    @Id
    private Long id;

    private String userId;

    private Long mentalHealthStepId;

    private LocalDateTime performedAt;
}
