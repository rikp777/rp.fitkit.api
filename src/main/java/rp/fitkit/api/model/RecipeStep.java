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
@Table("recipe_step")
public class RecipeStep implements Persistable<String> {

    @Id
    private String id;

    @Column("recipe_id")
    private String recipeId;

    @Column("step_number")
    private int stepNumber;

    @Transient
    private boolean isNew;

    public RecipeStep(String recipeId, int stepNumber) {
        this.id = UUID.randomUUID().toString();
        this.recipeId = recipeId;
        this.stepNumber = stepNumber;
        this.isNew = true;
    }

    @Override
    @Transient
    public boolean isNew() {
        return this.isNew || id == null;
    }

    public RecipeStep markAsNew() {
        this.isNew = true;
        return this;
    }
}
