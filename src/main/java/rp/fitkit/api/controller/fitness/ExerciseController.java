package rp.fitkit.api.controller.fitness;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import rp.fitkit.api.dto.CreateExerciseRequest;
import rp.fitkit.api.dto.ExerciseDto;
import rp.fitkit.api.dto.UpdateExerciseRequest;
import rp.fitkit.api.service.ExerciseService;

@RestController
@RequestMapping("/api/v1/exercises")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class ExerciseController {

    private final ExerciseService exerciseService;

    @GetMapping
    public Flux<ExerciseDto> getAllExercises(@RequestParam(defaultValue = "en") String lang) {
        return exerciseService.getAllExercises(lang);
    }

    @GetMapping("/{id}")
    public Mono<ExerciseDto> getExerciseById(
            @PathVariable String id,
            @RequestParam(defaultValue = "en") String lang) {
        return exerciseService.getExerciseById(id, lang);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ExerciseDto> createExercise(@Valid @RequestBody CreateExerciseRequest request) {
        return exerciseService.createExercise(request);
    }

    @PutMapping("/{id}")
    public Mono<ExerciseDto> updateExercise(
            @PathVariable String id,
            @Valid @RequestBody UpdateExerciseRequest request) {
        return exerciseService.updateExercise(id, request);
    }
}
