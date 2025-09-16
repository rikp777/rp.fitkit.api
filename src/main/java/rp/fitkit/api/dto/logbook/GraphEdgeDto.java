package rp.fitkit.api.dto.logbook;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GraphEdgeDto {
    private String source;
    private String target;
    private int weight;
}
