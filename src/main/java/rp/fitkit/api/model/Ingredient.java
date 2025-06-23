package rp.fitkit.api.model;

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
@Table("ingredient")
public class Ingredient implements Persistable<String> {

    @Id
    private String id;

    @Column("default_unit_id")
    private String defaultUnitId;

    @Transient
    private boolean isNew;

    public Ingredient(String defaultUnitId) {
        this.id = UUID.randomUUID().toString();
        this.defaultUnitId = defaultUnitId;
        this.isNew = true;
    }

    @Override
    @Transient
    public boolean isNew() {
        return this.isNew || id == null;
    }

    public Ingredient markAsNew() {
        this.isNew = true;
        return this;
    }
}

