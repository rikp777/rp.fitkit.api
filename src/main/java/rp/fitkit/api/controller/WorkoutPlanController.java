package rp.fitkit.api.controller;

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
import rp.fitkit.api.model.User;
import rp.fitkit.api.service.WorkoutPlanService;

@RestController
@RequestMapping("/api/v1/plans")
@SecurityRequirement(name = "bearerAuth")
public class WorkoutPlanController {

    private final WorkoutPlanService planService;

    @Autowired
    public WorkoutPlanController(WorkoutPlanService planService) {
        this.planService = planService;
    }

    /**
     * Endpoint om een nieuw, compleet workoutplan aan te maken voor de ingelogde gebruiker.
     * @param userDetails De details van de ingelogde gebruiker (automatisch geleverd door Spring Security).
     * @param planDto Het DTO met de volledige planstructuur uit de request body.
     * @return Een Mono die de aangemaakte WorkoutPlanDto bevat, met HTTP status 201 (Created).
     */
    @PostMapping
    public Mono<ResponseEntity<WorkoutPlanDto>> createWorkoutPlan(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody WorkoutPlanDto planDto) {

        String userId = ((User) userDetails).getId();
        return planService.createWorkoutPlan(planDto, userId)
                .map(savedPlan -> ResponseEntity.status(HttpStatus.CREATED).body(savedPlan));
    }

    /**
     * Endpoint om alle workoutplannen van de ingelogde gebruiker op te halen.
     */
    @GetMapping
    public Flux<WorkoutPlanDto> getPlansForCurrentUser(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String userId = ((User) userDetails).getId();
        return planService.getPlansForUser(userId);
    }

    /**
     * Endpoint om één specifiek workoutplan van de ingelogde gebruiker op te halen.
     */
    @GetMapping("/{planId}")
    public Mono<ResponseEntity<WorkoutPlanDto>> getPlanById(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String planId
    ) {

        String userId = ((User) userDetails).getId();
        return planService.getPlanByIdAndUser(planId, userId)
                .map(planDto -> ResponseEntity.ok(planDto))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
