package rp.fitkit.api.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@Table("recipe")
public class Recipe implements Persistable<String> {

    @Id
    private String id;

    @Column("user_id")
    private String userId; // Can be null if it's a system-provided recipe

    @Column("prep_time_minutes")
    private Integer prepTimeMinutes;

    @Column("cook_time_minutes")
    private Integer cookTimeMinutes;

    private Integer servings;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Transient
    private boolean isNew;

    public Recipe(String userId, Integer prepTimeMinutes, Integer cookTimeMinutes, Integer servings) {
        this.id = UUID.randomUUID().toString();
        this.userId = userId;
        this.prepTimeMinutes = prepTimeMinutes;
        this.cookTimeMinutes = cookTimeMinutes;
        this.servings = servings;
        this.createdAt = LocalDateTime.now();
        this.isNew = true;
    }

    @Override
    @Transient
    public boolean isNew() {
        return this.isNew || id == null;
    }

    public Recipe markAsNew() {
        this.isNew = true;
        return this;
    }
}
