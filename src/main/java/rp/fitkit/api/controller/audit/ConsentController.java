package rp.fitkit.api.controller.audit;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import rp.fitkit.api.dto.audit.Consent;
import rp.fitkit.api.dto.audit.ConsentRequestDto;
import rp.fitkit.api.dto.audit.ConsentResponseDto;
import rp.fitkit.api.model.user.User;
import rp.fitkit.api.repository.audit.ConsentRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@RestController
@RequestMapping("/api/v1/consent")
@RequiredArgsConstructor
public class ConsentController {

    private final ConsentRepository consentRepository;

    @PostMapping("/grant")
    public Mono<ConsentResponseDto> grantConsent(
            @AuthenticationPrincipal User user,
            @RequestBody ConsentRequestDto request
    ) {
        Consent consent = new Consent(
                user.getId(),
                request.getJustification(),
                Instant.now().plus(request.getDurationHours(), ChronoUnit.HOURS)
        );
        return consentRepository.save(consent)
                .map(saved -> new ConsentResponseDto(
                        saved.getId(),
                        saved.getJustification(),
                        saved.getExpiresAt()
                ));
    }
}

