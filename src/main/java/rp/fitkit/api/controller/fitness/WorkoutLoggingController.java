package rp.fitkit.api.controller.fitness;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import rp.fitkit.api.dto.ExerciseLogDto;
import rp.fitkit.api.dto.ExerciseSessionResponseDto;
import rp.fitkit.api.model.exercise.ExerciseSession;
import rp.fitkit.api.model.user.User;
import rp.fitkit.api.service.WorkoutLoggingService;

@RestController
@RequestMapping("/api/v1/log")
@AllArgsConstructor
public class WorkoutLoggingController {

    private final WorkoutLoggingService loggingService;
    private final Flux<ExerciseSessionResponseDto> workoutSessionStream;

    @PostMapping("/workout")
    @SecurityRequirement(name = "bearerAuth")
    public Mono<ResponseEntity<ExerciseSession>> logWorkout(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ExerciseLogDto logDto
    ) {
        String userId = ((User) userDetails).getId();
        return loggingService.logWorkoutSession(userId, logDto)
                .map(savedSession -> ResponseEntity.status(HttpStatus.CREATED).body(savedSession));
    }

    @GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ExerciseSessionResponseDto> streamWorkoutSessions() {
        return this.workoutSessionStream;
    }
}
