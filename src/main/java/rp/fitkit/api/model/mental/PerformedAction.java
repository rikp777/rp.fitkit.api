package rp.fitkit.api.model.mental;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Table("performed_action")
@AllArgsConstructor
@NoArgsConstructor
public class PerformedAction {
    @Id
    private Long id;

    private UUID userId;

    private Long mentalHealthStepId;

    private LocalDateTime performedAt;
}
