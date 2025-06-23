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
@Table("recipe_ingredient")
public class RecipeIngredient implements Persistable<String> {

    @Id
    private String id;

    @Column("recipe_id")
    private String recipeId;

    @Column("ingredient_id")
    private String ingredientId;

    private double amount;

    @Column("unit_id")
    private String unitId;

    @Transient
    private boolean isNew;

    public RecipeIngredient(String recipeId, String ingredientId, double amount, String unitId) {
        this.id = UUID.randomUUID().toString();
        this.recipeId = recipeId;
        this.ingredientId = ingredientId;
        this.amount = amount;
        this.unitId = unitId;
        this.isNew = true;
    }

    @Override
    @Transient
    public boolean isNew() {
        return this.isNew || id == null;
    }

    public RecipeIngredient markAsNew() {
        this.isNew = true;
        return this;
    }
}

