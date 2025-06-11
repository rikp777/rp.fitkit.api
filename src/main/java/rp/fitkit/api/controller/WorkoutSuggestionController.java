package rp.fitkit.api.controller;


import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.AllArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import rp.fitkit.api.model.User;
import rp.fitkit.api.model.WorkoutSuggestion;
import rp.fitkit.api.service.WorkoutSuggestionService;

@RestController
@RequestMapping("/api/v1/suggestions")
@AllArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class WorkoutSuggestionController {

    private final WorkoutSuggestionService suggestionService;

    @GetMapping("/planned")
    public Mono<WorkoutSuggestion> getPlannedWorkoutSuggestion(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String exerciseTemplateId
    ) {
        String userId = ((User) userDetails).getId();
        return suggestionService.getSuggestionForPlannedExercise(userId, exerciseTemplateId);
    }

    @GetMapping("/adhoc")
    public Mono<WorkoutSuggestion> getAdHocWorkoutSuggestion(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String exerciseName
    ) {
        String userId = ((User) userDetails).getId();
        return suggestionService.getSuggestionForAdHocExercise(userId, exerciseName);
    }
}

