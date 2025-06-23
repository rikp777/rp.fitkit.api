package rp.fitkit.api.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "user_settings")
@Data
@NoArgsConstructor
public class UserSettings {

    @Id
    private String userId;

    private ProgressionStrategy progressionStrategy; // enum

    @Column("rpe_low_override")
    private Double rpeLowOverride;

    @Column("rpe_high_override")
    private Double rpeHighOverride;
}
