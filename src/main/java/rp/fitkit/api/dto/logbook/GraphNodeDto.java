package rp.fitkit.api.dto.logbook;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GraphNodeDto {
    private String id;
    private String label;
    private int weight;
}
