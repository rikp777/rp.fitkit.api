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
@Table("product_version")
public class ProductVersion implements Persistable<String> {

    @Id
    private String id;

    @Column("product_id")
    private String productId;

    @Column("version_number")
    private int versionNumber;

    private String brand;

    @Column("category_id")
    private String categoryId;

    @Column("effective_from")
    private LocalDate effectiveFrom;

    private String notes;

    @Transient
    private boolean isNew;

    public ProductVersion(String productId, int versionNumber, String brand, String categoryId, LocalDate effectiveFrom, String notes) {
        this.id = UUID.randomUUID().toString();
        this.productId = productId;
        this.versionNumber = versionNumber;
        this.brand = brand;
        this.categoryId = categoryId;
        this.effectiveFrom = effectiveFrom;
        this.notes = notes;
        this.isNew = true;
    }

    @Override
    @Transient
    public boolean isNew() {
        return this.isNew || id == null;
    }

    public ProductVersion markAsNew() {
        this.isNew = true;
        return this;
    }
}
