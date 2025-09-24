package rp.fitkit.api.controller.fitness;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import rp.fitkit.api.dto.WorkoutPlanDto;
import rp.fitkit.api.model.user.User;
import rp.fitkit.api.service.WorkoutPlanService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/plans")
@SecurityRequirement(name = "bearerAuth")
public class WorkoutPlanController {

    private final WorkoutPlanService planService;

    @Autowired
    public WorkoutPlanController(WorkoutPlanService planService) {
        this.planService = planService;
    }

    @PostMapping
    public Mono<ResponseEntity<WorkoutPlanDto>> createWorkoutPlan(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody WorkoutPlanDto planDto) {

        UUID userId = ((User) userDetails).getId();
        return planService.createWorkoutPlan(planDto, userId)
                .map(savedPlan -> ResponseEntity.status(HttpStatus.CREATED).body(savedPlan));
    }

    @GetMapping
    public Flux<WorkoutPlanDto> getPlansForCurrentUser(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID userId = ((User) userDetails).getId();
        return planService.getPlansForUser(userId);
    }

    @GetMapping("/{planId}")
    public Mono<ResponseEntity<WorkoutPlanDto>> getPlanById(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID planId
    ) {

        UUID userId = ((User) userDetails).getId();
        return planService.getPlanByIdAndUser(planId, userId)
                .map(planDto -> ResponseEntity.ok(planDto))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{planId}")
    @SecurityRequirement(name = "bearerAuth")
    public Mono<ResponseEntity<Void>> deletePlan(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID planId) {

        UUID userId = ((User) userDetails).getId();
        return planService.deletePlan(planId, userId)
                .thenReturn(ResponseEntity.noContent().build());
    }
}
