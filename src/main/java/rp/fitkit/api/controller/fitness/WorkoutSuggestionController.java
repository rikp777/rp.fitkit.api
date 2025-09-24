package rp.fitkit.api.controller.fitness;


import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import rp.fitkit.api.model.user.User;
import rp.fitkit.api.model.WorkoutSuggestion;
import rp.fitkit.api.service.WorkoutSuggestionService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/suggestions")
@AllArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Validated
public class WorkoutSuggestionController {

    private final WorkoutSuggestionService suggestionService;

    @GetMapping("/planned")
    public Mono<WorkoutSuggestion> getPlannedWorkoutSuggestion(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam @NotBlank(message = "Exercise Template ID is required")
            UUID exerciseTemplateId
    ) {
        UUID userId = ((User) userDetails).getId();
        return suggestionService.getSuggestionForPlannedExercise(userId, exerciseTemplateId);
    }

    @GetMapping("/adhoc")
    public Mono<WorkoutSuggestion> getAdHocWorkoutSuggestion(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam @NotBlank(message = "Exercise Name is required")
            String exerciseName
    ) {
        UUID userId = ((User) userDetails).getId();
        return suggestionService.getSuggestionForAdHocExercise(userId, exerciseName);
    }
}

