package rp.fitkit.api.dto.logbook;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MoodStatsDto {
    private long positiveCount;
    private long neutralCount;
    private long negativeCount;
}
