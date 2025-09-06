package rp.fitkit.api.service.mental;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class AdminMentalHealthService {

    private final MentalHealthStepRepository stepRepository;
    private final MentalHealthStepTranslationRepository translationRepository;

    @Transactional
    public Mono<MentalHealthStep> createMentalHealthStep(CreateMentalHealthStepRequest request) {
        log.info("Admin creating new mental health step with number: {}", request.getStepNumber());

        MentalHealthStep newStep = new MentalHealthStep();
        newStep.setStepNumber(request.getStepNumber());
        newStep.setRequiredCompletions(request.getRequiredCompletions());

        return stepRepository.save(newStep)
                .doOnSuccess(savedStep -> log.info("Successfully created new step with ID: {}", savedStep.getId()))
                .flatMap(savedStep ->
                        Flux.fromIterable(request.getTranslations())
                                .flatMap(translationDto -> {
                                    log.debug("Saving translation for stepId: {} in language: {}", savedStep.getId(), translationDto.getLanguageCode());
                                    return translationRepository.save(
                                            new MentalHealthStepTranslation(
                                                    savedStep.getId(),
                                                    translationDto.getLanguageCode(),
                                                    translationDto.getTitle(),
                                                    translationDto.getDescription(),
                                                    translationDto.getPurpose()
                                            )
                                    );
                                })
                                .collectList()
                                .doOnSuccess(translations -> log.debug("Saved {} translations for stepId: {}", translations.size(), savedStep.getId()))
                                .thenReturn(savedStep)
                );
    }

    public Flux<MentalHealthStep> getAllMentalHealthSteps() {
        log.info("Admin fetching all mental health steps.");

        return stepRepository.findAll()
                .doOnComplete(() -> log.debug("Finished fetching all mental health steps."));
    }

    @Transactional
    public Mono<Void> deleteMentalHealthStep(Long stepId) {
        log.info("Admin attempting to delete mental health step with ID: {}", stepId);

        return stepRepository.findById(stepId)
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("Admin tried to delete non-existent mental health step with ID: {}", stepId);
                    return Mono.error(new ResourceNotFoundException("Step not found with id: " + stepId));
                }))
                .doOnSuccess(step -> log.debug("Found step to delete: {}. Proceeding with deletion.", step))
                .flatMap(stepRepository::delete)
                .doOnSuccess(v -> log.info("Successfully deleted mental health step with ID: {}", stepId));
    }
}

