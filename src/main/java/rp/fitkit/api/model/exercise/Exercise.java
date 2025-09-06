package rp.fitkit.api.model.exercise;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Data
@NoArgsConstructor
@Table("exercise")
public class Exercise implements Persistable<String> {

    @Id
    private String id;

    @Column("code")
    private String code;

    @Column("met_value")
    private double metValue;

    @Transient
    private boolean isNew;

    public Exercise(double metValue, String code) {
        this.id = UUID.randomUUID().toString();
        this.metValue = metValue;
        this.code = code;
        this.isNew = true;
    }


    @Override
    @Transient
    public boolean isNew() {
        return this.isNew || id == null;
    }

    public Exercise markAsNew() {
        this.isNew = true;
        return this;
    }
}


