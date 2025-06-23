package rp.fitkit.api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import rp.fitkit.api.dto.CreateExerciseRequest;
import rp.fitkit.api.dto.ExerciseDto;
import rp.fitkit.api.service.ExerciseService;

@RestController
@RequestMapping("/api/v1/exercises")
@RequiredArgsConstructor
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
    public Mono<ExerciseDto> createExercise(@RequestBody CreateExerciseRequest request) {
        return exerciseService.createExercise(request);
    }
}
