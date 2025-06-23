package rp.fitkit.api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import rp.fitkit.api.dto.CreateExerciseRequest;
import rp.fitkit.api.dto.ExerciseDto;
import rp.fitkit.api.dto.TranslationRequest;
import rp.fitkit.api.dto.UpdateExerciseRequest;
import rp.fitkit.api.model.Exercise;
import rp.fitkit.api.model.ExerciseTranslation;
import rp.fitkit.api.repository.ExerciseRepository;
import rp.fitkit.api.repository.ExerciseTranslationRepository;
import rp.fitkit.api.repository.LanguageRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExerciseService {

    private final ExerciseRepository exerciseRepository;
    private final ExerciseTranslationRepository exerciseTranslationRepository;
    private final LanguageRepository languageRepository; // Toegevoegd voor de nieuwe logica

    /**
     * Haalt alle oefeningen op en combineert ze met hun vertaling in de opgegeven taal.
     *
     * @param languageCode De gewenste taal voor de naam en beschrijving.
     * @return Een Flux van ExerciseDto's.
     */
    public Flux<ExerciseDto> getAllExercises(String languageCode) {
        return exerciseRepository.findAll()
                .flatMap(exercise -> findTranslationAndMapToDto(exercise, languageCode));
    }

    /**
     * Haalt één specifieke oefening op, gecombineerd met de vertaling in de opgegeven taal.
     *
     * @param id De ID van de oefening.
     * @param languageCode De gewenste taal.
     * @return Een Mono met de ExerciseDto, of een error als de oefening niet gevonden is.
     */
    public Mono<ExerciseDto> getExerciseById(String id, String languageCode) {
        return exerciseRepository.findById(id)
                .flatMap(exercise -> findTranslationAndMapToDto(exercise, languageCode))
                .switchIfEmpty(Mono.error(new RuntimeException("Exercise not found with id: " + id))); // Simpele error handling
    }

    /**
     * Creëert een nieuwe oefening en slaat alle meegeleverde vertalingen op.
     * Deze operatie is transactioneel: het slaagt volledig, of helemaal niet.
     *
     * @param request Het request object met de data voor de nieuwe oefening.
     * @return Een Mono met de DTO van de zojuist aangemaakte oefening, gebaseerd op de eerste vertaling in de lijst.
     */
    @Transactional
    public Mono<ExerciseDto> createExercise(CreateExerciseRequest request) {
        Exercise exercise = new Exercise(request.getMetValue(), request.getPrimaryMuscleGroup());
        exercise.markAsNew();

        return exerciseRepository.save(exercise)
                .flatMap(savedExercise -> {
                    Flux<ExerciseTranslation> saveTranslationsFlux = Flux.fromIterable(request.getTranslations())
                            .flatMap(translationRequest -> {
                                // 4. Maak en sla voor elk verzoek een ExerciseTranslation op
                                ExerciseTranslation translation = new ExerciseTranslation(
                                        savedExercise.getId(),
                                        translationRequest.getLanguageCode(),
                                        translationRequest.getName(),
                                        translationRequest.getDescription(),
                                        translationRequest.getInstructions()
                                );
                                return exerciseTranslationRepository.save(translation);
                            });

                    // 5. Voer het opslaan uit en map het resultaat naar een DTO.
                    //    We gebruiken de eerste vertaling uit het request voor de response DTO.
                    return saveTranslationsFlux.collectList().map(savedTranslations -> {
                        if (savedTranslations.isEmpty()) {
                            // Dit zou niet moeten gebeuren als de request validatie goed is, maar is een goede guard clause.
                            return toDto(savedExercise, new ExerciseTranslation(savedExercise.getId(), "", "No translation provided", "", ""));
                        }
                        // Gebruik de eerste opgeslagen vertaling voor de response.
                        return toDto(savedExercise, savedTranslations.get(0));
                    });
                });
    }

    @Transactional
    public Mono<ExerciseDto> updateExercise(String id, UpdateExerciseRequest request) {
        return exerciseRepository.findById(id)
                .flatMap(exercise -> {
                    // Werk de basis-eigenschappen van de oefening bij
                    exercise.setMetValue(request.getMetValue());
                    exercise.setPrimaryMuscleGroup(request.getPrimaryMuscleGroup());

                    // Sla de bijgewerkte oefening op en verwijder vervolgens de oude vertalingen
                    return exerciseRepository.save(exercise)
                            .flatMap(savedExercise ->
                                    exerciseTranslationRepository.deleteByExerciseId(savedExercise.getId())
                                            // Nadat de oude vertalingen zijn verwijderd, sla de nieuwe op
                                            .then(saveOrUpdateTranslations(savedExercise, request.getTranslations()))
                                            .map(savedTranslations -> toDto(savedExercise, savedTranslations.get(0)))
                            );
                })
                .switchIfEmpty(Mono.error(new RuntimeException("Exercise not found with id: " + id)));
    }

    private Mono<List<ExerciseTranslation>> saveOrUpdateTranslations(Exercise exercise, List<TranslationRequest> translations) {
        return Flux.fromIterable(translations)
                .flatMap(translationRequest -> {
                    ExerciseTranslation translation = new ExerciseTranslation(
                            exercise.getId(),
                            translationRequest.getLanguageCode(),
                            translationRequest.getName(),
                            translationRequest.getDescription(),
                            translationRequest.getInstructions()
                    );
                    return exerciseTranslationRepository.save(translation);
                })
                .collectList();
    }

    /**
     * Hulpfunctie om een Exercise te combineren met zijn vertaling.
     * Zoekt de vertaling op en combineert de twee objecten in een DTO.
     */
    private Mono<ExerciseDto> findTranslationAndMapToDto(Exercise exercise, String languageCode) {
        return exerciseTranslationRepository.findByExerciseIdAndLanguageCode(exercise.getId(), languageCode)
                .defaultIfEmpty(new ExerciseTranslation(exercise.getId(), languageCode, "", "", ""))
                .map(translation -> toDto(exercise, translation));
    }

    /**
     * Hulpfunctie die een Exercise en een ExerciseTranslation omzet naar een ExerciseDto.
     */
    private ExerciseDto toDto(Exercise exercise, ExerciseTranslation translation) {
        return new ExerciseDto(
                exercise.getId(),
                translation.getName(),
                translation.getDescription(),
                translation.getInstructions(),
                exercise.getMetValue(),
                exercise.getPrimaryMuscleGroup()
        );
    }
}