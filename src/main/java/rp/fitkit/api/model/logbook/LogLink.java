package rp.fitkit.api.model.logbook;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("log_links")
public class LogLink {
    @Id
    @Column("link_id")
    private Long id;

    @Column("source_section_id")
    private Long sourceSectionId;

    @Column("anchor_text")
    private String anchorText;

    @Column("target_log_id")
    private Long targetLogId;
}

