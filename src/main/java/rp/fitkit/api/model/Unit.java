package rp.fitkit.api.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@Table("unit")
public class Unit implements Persistable<String> {

    @Id
    private String id;

    @Transient
    private boolean isNew;

    public Unit(String id) {
        this.id = id;
        this.isNew = true;
    }

    @Override
    @Transient
    public boolean isNew() {
        return this.isNew || id == null;
    }

    public Unit markAsNew() {
        this.isNew = true;
        return this;
    }
}
