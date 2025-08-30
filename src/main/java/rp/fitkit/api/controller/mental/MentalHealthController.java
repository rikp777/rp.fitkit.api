package rp.fitkit.api.controller.mental;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
@SecurityRequirement(name = "bearerAuth")
public class MentalHealthController {

    private final MentalHealthService mentalHealthService;

    /**
     * Haalt de lijst met mentale gezondheidsstappen op voor de momenteel ingelogde gebruiker.
     * De unlock-status en voortgang worden per gebruiker berekend.
     *
     * @param userDetails  De details van de ingelogde gebruiker.
     * @param languageCode De taalcode voor de vertalingen (optioneel, standaard is "en-US").
     * @return Een Flux van DTO's die de stappen vertegenwoordigen.
     */
    @GetMapping("/steps")
    public Flux<MentalHealthStepDto> getMentalHealthSteps(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "en-US") String languageCode) {

        String userId = ((User) userDetails).getId();
        return mentalHealthService.getMentalHealthStepsForUser(userId, languageCode);
    }

    /**
     * Registreert dat de ingelogde gebruiker een specifieke stap heeft voltooid.
     *
     * @param userDetails De details van de ingelogde gebruiker.
     * @param stepId      De ID van de stap die is voltooid.
     * @return Een Mono met een 204 No Content response bij succes.
     */
    @PostMapping("/steps/{stepId}/perform")
    public Mono<ResponseEntity<Void>> performStepAction(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long stepId) {

        String userId = ((User) userDetails).getId();
        return mentalHealthService.performStepAction(userId, stepId)
                .then(Mono.just(ResponseEntity.noContent().build()));
    }
}

