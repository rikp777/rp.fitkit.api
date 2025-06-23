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
@Table("product")
public class Product implements Persistable<String> {

    @Id
    private String id;

    @Column("user_id")
    private String userId; // Can be null for system-defined products

    @Column("base_name")
    private String baseName;

    @Transient
    private boolean isNew;

    public Product(String userId, String baseName) {
        this.id = UUID.randomUUID().toString();
        this.userId = userId;
        this.baseName = baseName;
        this.isNew = true;
    }

    @Override
    @Transient
    public boolean isNew() {
        return this.isNew || id == null;
    }

    public Product markAsNew() {
        this.isNew = true;
        return this;
    }
}
