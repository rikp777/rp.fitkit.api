package rp.fitkit.api.dto.logbook;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import rp.fitkit.api.model.root.SectionType;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LinkPreviewDto {
    // Info over de BRON van de link
    private Long sourceLogId;
    private LocalDate sourceLogDate;
    private SectionType sourceSectionType;

    // Info over de link zelf
    private String anchorText;
    private String contextSnippet;

    // Info over het DOEL van de link
    private Long targetLogId;
    private LocalDate targetLogDate;
}

