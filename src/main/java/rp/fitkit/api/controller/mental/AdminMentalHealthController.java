package rp.fitkit.api.controller.mental;

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
@PreAuthorize("hasRole('ADMIN')")
public class AdminMentalHealthController implements AdminMentalHealthApi {

    private final AdminMentalHealthService adminMentalHealthService;

    @Override
    public Mono<ResponseEntity<MentalHealthStep>> createMentalHealthStep(CreateMentalHealthStepRequest request) {
        return adminMentalHealthService.createMentalHealthStep(request)
                .map(step -> ResponseEntity.status(HttpStatus.CREATED).body(step));
    }

    @Override
    public Flux<MentalHealthStep> getAllMentalHealthSteps() {
        return adminMentalHealthService.getAllMentalHealthSteps();
    }

    @Override
    public Mono<ResponseEntity<Void>> deleteMentalHealthStep(Long stepId) {
        return adminMentalHealthService.deleteMentalHealthStep(stepId)
                .thenReturn(ResponseEntity.noContent().build());
    }
}


