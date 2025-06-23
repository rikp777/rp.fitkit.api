package rp.fitkit.api.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("product_ingredient")
public class ProductIngredient {

    @Column("product_version_id")
    private String productVersionId;

    @Column("ingredient_id")
    private String ingredientId;

    private double amount;

    @Column("unit_id")
    private String unitId;
}
