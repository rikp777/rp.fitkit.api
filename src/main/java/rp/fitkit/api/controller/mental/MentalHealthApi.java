package rp.fitkit.api.controller.mental;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import rp.fitkit.api.dto.mental.MentalHealthStepDto;

@Tag(name = "Mental Health (User)", description = "Endpoints for regular users of the Mental Health feature.")
@SecurityRequirement(name = "bearerAuth")
public interface MentalHealthApi {

    /**
     * Haalt de lijst met mentale gezondheidsstappen op voor de momenteel ingelogde gebruiker.
     * De unlock-status en voortgang worden per gebruiker berekend.
     *
     * @param userDetails  De details van de ingelogde gebruiker.
     * @param languageCode De taalcode voor de vertalingen (optioneel, standaard is "en-US").
     * @return Een Flux van DTO's die de stappen vertegenwoordigen.
     */
    @GetMapping("/steps")
    Flux<MentalHealthStepDto> getMentalHealthSteps(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "en-US") String languageCode);

    /**
     * Registreert dat de ingelogde gebruiker een specifieke stap heeft voltooid.
     *
     * @param userDetails De details van de ingelogde gebruiker.
     * @param stepId      De ID van de stap die is voltooid.
     * @return Een Mono met een 204 No Content response bij succes.
     */
    @PostMapping("/steps/{stepId}/perform")
    Mono<ResponseEntity<Void>> performStepAction(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long stepId);
}

