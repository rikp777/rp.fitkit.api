package rp.fitkit.api.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@Table("user_metric")
public class UserMetric implements Persistable<String> {

    @Id
    private String id;

    @Column("user_id")
    private String userId;

    @Column("date_recorded")
    private LocalDate dateRecorded;

    @Column("body_weight_kg")
    private Double bodyWeightKg;

    @Column("height_cm")
    private Double heightCm;

    @Transient
    private boolean isNew;

    public UserMetric(String userId, LocalDate dateRecorded, Double bodyWeightKg, Double heightCm) {
        this.id = UUID.randomUUID().toString();
        this.userId = userId;
        this.dateRecorded = dateRecorded;
        this.bodyWeightKg = bodyWeightKg;
        this.heightCm = heightCm;
        this.isNew = true;
    }

    @Override
    @Transient
    public boolean isNew() {
        return this.isNew || id == null;
    }

    public UserMetric markAsNew() {
        this.isNew = true;
        return this;
    }
}
