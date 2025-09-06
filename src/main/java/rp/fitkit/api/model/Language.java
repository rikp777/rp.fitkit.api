package rp.fitkit.api.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@Table("language")
public class Language implements Persistable<String> {

    @Id
    private String code;

    private String name;

    @Transient
    private boolean isNew;

    public Language(String code, String name) {
        this.code = code;
        this.name = name;
        this.isNew = true;
    }

    @Override
    @Transient
    public boolean isNew() {
        return this.isNew || code == null;
    }

    public Language markAsNew() {
        this.isNew = true;
        return this;
    }

    @Override
    public String getId() {
        return code;
    }
}
