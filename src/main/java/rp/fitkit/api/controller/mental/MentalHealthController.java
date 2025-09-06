package rp.fitkit.api.controller.mental;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import rp.fitkit.api.dto.mental.MentalHealthStepDto;
import rp.fitkit.api.model.user.User;
import rp.fitkit.api.service.mental.MentalHealthService;

@RestController
@RequestMapping("/api/v1/mental-health")
@RequiredArgsConstructor
public class MentalHealthController implements MentalHealthApi {

    private final MentalHealthService mentalHealthService;

    @Override
    public Flux<MentalHealthStepDto> getMentalHealthSteps(UserDetails userDetails, String languageCode) {

        String userId = ((User) userDetails).getId();
        return mentalHealthService.getMentalHealthStepsForUser(userId, languageCode);
    }

    @Override
    public Mono<MentalHealthStepDto> getSuggestedMentalHealthStep(UserDetails userDetails, String languageCode) {
        String userId = ((User) userDetails).getId();
        return mentalHealthService.getSuggestedStepForUser(userId, languageCode);
    }

    @Override
    public Mono<MentalHealthStepDto> getMentalHealthStepById(UserDetails userDetails, Long stepId, String languageCode) {
        String userId = ((User) userDetails).getId();
        return mentalHealthService.getMentalHealthStepForUser(userId, stepId, languageCode);
    }

    @Override
    public Mono<ResponseEntity<Void>> performStepAction(UserDetails userDetails, Long stepId) {

        String userId = ((User) userDetails).getId();
        return mentalHealthService.performStepAction(userId, stepId)
                .then(Mono.just(ResponseEntity.noContent().build()));
    }
}