package rp.fitkit.api.dto.logbook;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class GraphDataDto {
    private List<GraphNodeDto> nodes;
    private List<GraphEdgeDto> edges;
}