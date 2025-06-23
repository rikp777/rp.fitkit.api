package rp.fitkit.api.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("product_version_translation")
public class ProductVersionTranslation {

    @Column("product_version_id")
    private String productVersionId;

    @Column("language_code")
    private String languageCode;

    private String name;
}
