package rp.fitkit.api.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import rp.fitkit.api.dto.CreateMuscleGroupRequest;
import rp.fitkit.api.dto.MuscleGroupDto;
import rp.fitkit.api.dto.MuscleGroupServiceResponse;
import rp.fitkit.api.dto.MuscleGroupsResponse;
import rp.fitkit.api.service.MuscleGroupService;

@RestController
@RequestMapping("/api/muscle-groups")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Validated
public class MuscleGroupController {

    private final MuscleGroupService muscleGroupService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<MuscleGroupDto> createMuscleGroup(@Valid @RequestBody CreateMuscleGroupRequest request) {
        return muscleGroupService.createMuscleGroup(request);
    }

    @GetMapping
    public Flux<MuscleGroupDto> getAllMuscleGroups(
            @RequestHeader(name = "Accept-Language", defaultValue = "en-GB")
            @NotBlank(message = "Language code cannot be blank") String languageCode,
            ServerWebExchange exchange
    ) {
        exchange.getResponse().getHeaders().add("Content-Language", languageCode);
        return muscleGroupService.getAllMuscleGroups(languageCode)
                .map(MuscleGroupServiceResponse::getMuscleGroupDto);
    }

    @GetMapping("/with-omissions") // Nieuw endpoint
    public Mono<MuscleGroupsResponse> getAllMuscleGroupsWithOmissions(
            @RequestHeader(name = "Accept-Language", defaultValue = "en-GB")
            @NotBlank(message = "Language code cannot be blank") String languageCode,
            ServerWebExchange exchange
    ) {
        exchange.getResponse().getHeaders().add("Content-Language", languageCode);
        return muscleGroupService.getAllMuscleGroupsWithOmissions(languageCode);
    }


    @GetMapping("/{id}")
    public Mono<MuscleGroupDto> getMuscleGroupById(
            @PathVariable(name = "id")
            @NotBlank(message = "Muscle group ID cannot be blank") String id,
            @RequestHeader(name = "Accept-Language", defaultValue = "en-GB")
            @NotBlank(message = "Language code cannot be blank") String languageCode,
            ServerWebExchange exchange
    ) {
        return muscleGroupService.getMuscleGroupByIdAndLanguageCode(id, languageCode)
                .doOnNext(response -> exchange.getResponse().getHeaders().add("Content-Language", response.getContentLanguage()))
                .map(MuscleGroupServiceResponse::getMuscleGroupDto);
    }

    @GetMapping("/code/{code}")
    public Mono<MuscleGroupDto> getMuscleGroupByCode(
            @PathVariable(name = "code")
            @NotBlank(message = "Muscle group Code cannot be blank") String code,
            @RequestHeader(name = "Accept-Language", defaultValue = "en-GB")
            @NotBlank(message = "Language code cannot be blank") String languageCode,
            ServerWebExchange exchange) {
        return muscleGroupService.getMuscleGroupByCodeAndLanguageCode(code, languageCode)
                .doOnNext(response -> exchange.getResponse().getHeaders().add("Content-Language", response.getContentLanguage()))
                .map(MuscleGroupServiceResponse::getMuscleGroupDto);
    }
}
