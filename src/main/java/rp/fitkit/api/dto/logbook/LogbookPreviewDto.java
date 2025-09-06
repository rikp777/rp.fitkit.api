package rp.fitkit.api.dto.logbook;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogbookPreviewDto {
    private Long logId;
    private LocalDate logDate;
    private String summaryPreview;
}

