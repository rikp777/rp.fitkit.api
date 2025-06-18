package rp.fitkit.api.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import rp.fitkit.api.dto.ProgressDataPointDto;
import rp.fitkit.api.model.User;
import rp.fitkit.api.service.StatisticsService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/stats")
public class StatisticsController {

    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping("/progress/{exerciseName}")
    public Mono<List<ProgressDataPointDto>> getProgressChartData(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String exerciseName
    ) {
        log.info("Fetching progress chart data for exercise: {}", exerciseName);
        String userId = ((User) userDetails).getId();
        return statisticsService.getEstimated1rmHistory(userId, exerciseName)
                .collectList();
    }
}

