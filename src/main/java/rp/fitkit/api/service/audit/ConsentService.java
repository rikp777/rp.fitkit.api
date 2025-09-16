package rp.fitkit.api.service.audit;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import rp.fitkit.api.dto.audit.Consent;
import rp.fitkit.api.exception.ConsentException;
import rp.fitkit.api.repository.audit.ConsentRepository;
import rp.fitkit.api.repository.user.UserRepository;

@Service
@RequiredArgsConstructor
public class ConsentService {

    private final ConsentRepository consentRepository;
    private final UserRepository userRepository;

    public Mono<Consent> findAndValidateConsent(String justification, String targetUsername) {
        return consentRepository.findByJustification(justification)
                .switchIfEmpty(Mono.error(new ConsentException("No consent found for justification: " + justification)))
                .flatMap(consent -> {
                    if (!consent.isValid()) {
                        return Mono.error(new ConsentException("Consent has expired or been revoked."));
                    }
                    return userRepository.findById(consent.getUserId())
                            .switchIfEmpty(Mono.error(new ConsentException("User who granted consent not found.")))
                            .flatMap(consentingUser -> {
                                if (!consentingUser.getUsername().equals(targetUsername)) {
                                    return Mono.error(new ConsentException("Consent was granted by user '" + consentingUser.getUsername() + "' but the target is '" + targetUsername + "'."));
                                }
                                return Mono.just(consent);
                            });
                });
    }
}

