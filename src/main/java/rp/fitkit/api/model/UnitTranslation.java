package rp.fitkit.api.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("unit_translation")
public class UnitTranslation {

    @Column("unit_id")
    private String unitId;

    @Column("language_code")
    private String languageCode;

    @Column("name_singular")
    private String nameSingular;

    @Column("name_plural")
    private String namePlural;
}
