package rp.fitkit.api.dto.logbook;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import rp.fitkit.api.model.root.SectionType;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogSectionDto {
    private SectionType sectionType;
    private String summary;
    private String mood;
}

