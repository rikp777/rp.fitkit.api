package rp.fitkit.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import rp.fitkit.api.dto.ExerciseLogDto;
import rp.fitkit.api.model.ExerciseSession;
import rp.fitkit.api.model.WorkoutSuggestion;
import rp.fitkit.api.service.WorkoutSuggestionService;

@RestController
@RequestMapping("/api/v1/workouts")
public class WorkoutController {

    private final WorkoutSuggestionService suggestionService;

    @Autowired // Spring zal automatisch een instantie van WorkoutSuggestionService injecteren
    public WorkoutController(WorkoutSuggestionService suggestionService) {
        this.suggestionService = suggestionService;
    }

    /**
     * Endpoint om een workout suggestie op te halen op een reactieve manier.
     * Voorbeeld request: GET /api/v1/workouts/suggestion?userId=user123&exerciseName=Bench%20Press
     * (We passen het bestaande endpoint aan voor dit voorbeeld)
     *
     * @param userId De ID van de gebruiker.
     * @param exerciseName De naam van de oefening waarvoor een suggestie wordt gevraagd.
     * @return Een Mono die de WorkoutSuggestion zal emitteren.
     */
    @GetMapping("/suggestion")
    public Mono<WorkoutSuggestion> getWorkoutSuggestionReactive(
            @RequestParam String userId,
            @RequestParam String exerciseName) {
        return suggestionService.generateSuggestionReactive(userId, exerciseName);
    }

    /**
     * Endpoint om een nieuwe workout sessie te loggen.
     * Voorbeeld request: POST /api/v1/users/{userId}/workouts/log
     * met een JSON body die ExerciseLogDto representeert.
     *
     * @param userId De ID van de gebruiker.
     * @param logDto De workout details uit de request body.
     * @return Een Mono die de aangemaakte ExerciseSession bevat, met HTTP status 201 (Created).
     */
    @PostMapping("/users/{userId}/log") // We halen userId nu uit het pad
    public Mono<ResponseEntity<ExerciseSession>> logWorkout(
            @PathVariable String userId,
            @RequestBody ExerciseLogDto logDto) {
        return suggestionService.logWorkoutSessionReactive(userId, logDto)
                .map(savedSession -> ResponseEntity.status(HttpStatus.CREATED).body(savedSession));
    }
}
