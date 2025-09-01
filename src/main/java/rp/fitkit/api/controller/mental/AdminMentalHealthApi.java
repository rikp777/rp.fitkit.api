package rp.fitkit.api.controller.mental;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import rp.fitkit.api.dto.mental.request.CreateMentalHealthStepRequest;
import rp.fitkit.api.model.mental.MentalHealthStep;

@Tag(name = "Mental Health (Admin)", description = "Admin-only endpoints for managing the Mental Health feature.")
@SecurityRequirement(name = "bearerAuth")
public interface AdminMentalHealthApi {

    @PostMapping("/steps")
    Mono<ResponseEntity<MentalHealthStep>> createMentalHealthStep(@Valid @RequestBody CreateMentalHealthStepRequest request);

    @GetMapping("/steps")
    Flux<MentalHealthStep> getAllMentalHealthSteps();

    @DeleteMapping("/steps/{stepId}")
    Mono<ResponseEntity<Void>> deleteMentalHealthStep(@PathVariable Long stepId);
}

