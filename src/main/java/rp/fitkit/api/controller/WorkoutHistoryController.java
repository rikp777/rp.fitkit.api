package rp.fitkit.api.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.AllArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import rp.fitkit.api.dto.ExerciseSessionResponseDto;
import rp.fitkit.api.model.user.User;
import rp.fitkit.api.service.WorkoutHistoryService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/history")
@AllArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class WorkoutHistoryController {

    private final WorkoutHistoryService historyService;

    @GetMapping
    public Mono<Map<LocalDate, List<ExerciseSessionResponseDto>>> getFullWorkoutHistory(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String userId = ((User) userDetails).getId();
        return historyService.getFullHistoryGroupedByDate(userId);
    }

    @GetMapping("/{exerciseName}")
    public Flux<ExerciseSessionResponseDto> getWorkoutHistoryForExercise(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String exerciseName
    ) {
        String userId = ((User) userDetails).getId();
        return historyService.getHistoryForExercise(userId, exerciseName);
    }
}
