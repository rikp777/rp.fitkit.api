package rp.fitkit.api.model.muscleGroup;

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
@Table("muscle_group")
public class MuscleGroup implements Persistable<String> {

    @Id
    private String id;

    @Column("code")
    private String code;

    @Column("latin_name")
    private String latinName;

    @Transient
    private boolean isNew;

    public MuscleGroup(String code, String latinName) {
        this.id = UUID.randomUUID().toString();
        this.code = code;
        this.latinName = latinName;
        this.isNew = true;
    }

    @Override
    @Transient
    public boolean isNew() {
        return this.isNew || id == null;
    }

    public MuscleGroup markAsNew() {
        this.isNew = true;
        return this;
    }
}

