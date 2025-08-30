package rp.fitkit.api.repository.mental;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import rp.fitkit.api.model.mental.translation.MentalHealthStepTranslation;

@Repository
public interface MentalHealthStepTranslationRepository extends R2dbcRepository<MentalHealthStepTranslation, Long> {

    /**
     * Zoekt alle vertalingen voor een specifieke mentale gezondheidsstap.
     *
     * @param mentalHealthStepId De ID van de stap.
     * @return Een Flux met de gevonden vertalingen.
     */
    Flux<MentalHealthStepTranslation> findByMentalHealthStepId(Long mentalHealthStepId);

    /**
     * Zoekt een specifieke vertaling op basis van de stap ID en de taalcode.
     *
     * @param mentalHealthStepId De ID van de stap.
     * @param languageCode De code van de taal.
     * @return Een Mono met de gevonden vertaling, of een lege Mono als deze niet bestaat.
     */
    Mono<MentalHealthStepTranslation> findByMentalHealthStepIdAndLanguageCode(Long mentalHealthStepId, String languageCode);
}

