package rp.fitkit.api.controller.mental;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import rp.fitkit.api.dto.mental.request.CreateMentalHealthStepRequest;
import rp.fitkit.api.model.mental.MentalHealthStep;
import rp.fitkit.api.service.mental.AdminMentalHealthService;

@RestController
@RequestMapping("/api/v1/admin/mental-health")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Mental Health (Admin)", description = "Admin-only endpoints for managing the Mental Health feature.")
public class AdminMentalHealthController {

    private final AdminMentalHealthService adminMentalHealthService;

    @PostMapping("/steps")
    public Mono<ResponseEntity<MentalHealthStep>> createMentalHealthStep(@Valid @RequestBody CreateMentalHealthStepRequest request) {
        return adminMentalHealthService.createMentalHealthStep(request)
                .map(step -> ResponseEntity.status(HttpStatus.CREATED).body(step));
    }

    @GetMapping("/steps")
    public Flux<MentalHealthStep> getAllMentalHealthSteps() {
        return adminMentalHealthService.getAllMentalHealthSteps();
    }

    @DeleteMapping("/steps/{stepId}")
    public Mono<ResponseEntity<Void>> deleteMentalHealthStep(@PathVariable Long stepId) {
        return adminMentalHealthService.deleteMentalHealthStep(stepId)
                .thenReturn(ResponseEntity.noContent().build());
    }
}

