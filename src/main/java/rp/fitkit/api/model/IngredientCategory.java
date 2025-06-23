package rp.fitkit.api.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("ingredient_category")
public class IngredientCategory {

    @Column("ingredient_id")
    private String ingredientId;

    @Column("category_id")
    private String categoryId;
}
