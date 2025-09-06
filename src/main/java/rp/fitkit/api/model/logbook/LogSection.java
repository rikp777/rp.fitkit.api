package rp.fitkit.api.model.logbook;

import lombok.Data;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import rp.fitkit.api.model.root.SectionType;

@Data
@Table("log_sections")
public class LogSection {
    @Id
    @Column("section_id")
    private Long id;

    @Column("log_id")
    private Long dailyLogId;

    @Column("section_type")
    private SectionType sectionType;

    private String summary;

    private String mood;
}

