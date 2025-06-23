package rp.fitkit.api.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("category_translation")
public class CategoryTranslation {

    @Column("category_id")
    private String categoryId;

    @Column("language_code")
    private String languageCode;

    private String name;
}

