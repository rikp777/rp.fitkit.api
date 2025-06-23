package rp.fitkit.api.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("recipe_translation")
public class RecipeTranslation {

    @Column("recipe_id")
    private String recipeId;

    @Column("language_code")
    private String languageCode;

    private String name;
    private String description;
}
