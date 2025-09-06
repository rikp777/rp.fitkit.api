package rp.fitkit.api.dto.logbook;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FullLogbookDto {
    private Long logId;
    private LocalDate logDate;
    private List<LogSectionDto> sections;
    private List<LinkPreviewDto> outgoingLinks;
    private List<LinkPreviewDto> incomingLinks;
}

