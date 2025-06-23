package rp.fitkit.api.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("recipe_ingredient_translation")
public class RecipeIngredientTranslation {

    @Column("recipe_ingredient_id")
    private String recipeIngredientId;

    @Column("language_code")
    private String languageCode;

    private String notes;
}
