package rp.fitkit.api.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("recipe_step_translation")
public class RecipeStepTranslation {

    @Column("recipe_step_id")
    private String recipeStepId;

    @Column("language_code")
    private String languageCode;

    private String instructions;
}
