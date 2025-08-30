package rp.fitkit.api.service.mental;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import rp.fitkit.api.dto.mental.request.CreateMentalHealthStepRequest;
import rp.fitkit.api.exception.ResourceNotFoundException;
import rp.fitkit.api.model.mental.MentalHealthStep;
import rp.fitkit.api.model.mental.translation.MentalHealthStepTranslation;
import rp.fitkit.api.repository.mental.MentalHealthStepRepository;
import rp.fitkit.api.repository.mental.MentalHealthStepTranslationRepository;

@Service
@RequiredArgsConstructor
public class AdminMentalHealthService {

    private final MentalHealthStepRepository stepRepository;
    private final MentalHealthStepTranslationRepository translationRepository;

    @Transactional
    public Mono<MentalHealthStep> createMentalHealthStep(CreateMentalHealthStepRequest request) {
        MentalHealthStep newStep = new MentalHealthStep();
        newStep.setStepNumber(request.getStepNumber());
        newStep.setRequiredCompletions(request.getRequiredCompletions());

        return stepRepository.save(newStep)
                .flatMap(savedStep ->
                        Flux.fromIterable(request.getTranslations())
                                .flatMap(translationDto -> translationRepository.save(
                                        new MentalHealthStepTranslation(
                                                savedStep.getId(),
                                                translationDto.getLanguageCode(),
                                                translationDto.getTitle(),
                                                translationDto.getDescription(),
                                                translationDto.getPurpose()
                                        )
                                ))
                                .collectList()
                                .thenReturn(savedStep)
                );
    }

    public Flux<MentalHealthStep> getAllMentalHealthSteps() {
        return stepRepository.findAll();
    }

    @Transactional
    public Mono<Void> deleteMentalHealthStep(Long stepId) {
        // De 'deleteCascade' in de database handelt het verwijderen van vertalingen,
        // voortgang en acties automatisch af.
        return stepRepository.findById(stepId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Step not found with id: " + stepId)))
                .flatMap(stepRepository::delete);
    }
}

