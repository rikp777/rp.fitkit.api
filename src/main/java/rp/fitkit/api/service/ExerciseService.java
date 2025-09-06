package rp.fitkit.api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import rp.fitkit.api.dto.*;
import rp.fitkit.api.model.exercise.Exercise;
import rp.fitkit.api.model.exercise.ExerciseMuscleGroup;
import rp.fitkit.api.model.exercise.ExerciseTranslation;
import rp.fitkit.api.repository.ExerciseMuscleGroupRepository;
import rp.fitkit.api.repository.ExerciseRepository;
import rp.fitkit.api.repository.ExerciseTranslationRepository;
import rp.fitkit.api.repository.LanguageRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExerciseService {
    private final ExerciseRepository exerciseRepository;
    private final ExerciseTranslationRepository exerciseTranslationRepository;
    private final ExerciseMuscleGroupRepository exerciseMuscleGroupRepository;
    private final MuscleGroupService muscleGroupService;
    private final LanguageRepository languageRepository;

    public Flux<ExerciseDto> getAllExercises(String languageCode) {
        return exerciseRepository.findAll()
                .flatMap(exercise -> findTranslationAndMuscleGroupsAndMapToDto(exercise, languageCode));
    }

    public Mono<ExerciseDto> getExerciseById(String id, String languageCode) {
        return exerciseRepository.findById(id)
                .flatMap(exercise -> findTranslationAndMuscleGroupsAndMapToDto(exercise, languageCode))
                .switchIfEmpty(Mono.error(new RuntimeException("Exercise not found with id: " + id)));
    }

    @Transactional
    public Mono<ExerciseDto> createExercise(CreateExerciseRequest request) {
        Exercise exercise = new Exercise(request.getMetValue(), request.getCode());
        exercise.markAsNew();

        return exerciseRepository.save(exercise)
                .flatMap(savedExercise -> {
                    Mono<Void> saveTranslations = saveExerciseTranslations(savedExercise.getId(), request.getTranslations());
                    Mono<Void> saveMuscleGroupAssociations = saveExerciseMuscleGroupAssociations(savedExercise.getId(), request.getMuscleGroupCodes());

                    return Mono.when(saveTranslations, saveMuscleGroupAssociations)
                            .then(Mono.defer(() -> {
                                return findTranslationAndMuscleGroupsAndMapToDto(savedExercise,
                                        request.getTranslations().stream()
                                                .map(TranslationRequest::getLanguageCode)
                                                .findFirst()
                                                .orElse("en")
                                );
                            }));
                });
    }

    @Transactional
    public Mono<ExerciseDto> updateExercise(String id, UpdateExerciseRequest request) {
        return exerciseRepository.findById(id)
                .flatMap(exercise -> {
                    exercise.setMetValue(request.getMetValue());

                    return exerciseRepository.save(exercise)
                            .flatMap(updatedExercise -> {
                                Mono<Void> deleteOldTranslations = exerciseTranslationRepository.deleteByExerciseId(updatedExercise.getId());
                                Mono<Void> deleteOldMuscleGroupAssociations = exerciseMuscleGroupRepository.deleteByExerciseId(updatedExercise.getId());

                                Mono<Void> saveNewTranslations = saveExerciseTranslations(updatedExercise.getId(), request.getTranslations());
                                Mono<Void> saveNewMuscleGroupAssociations = saveExerciseMuscleGroupAssociations(updatedExercise.getId(), request.getMuscleGroupCodes());

                                return Mono.when(deleteOldTranslations, deleteOldMuscleGroupAssociations)
                                        .then(Mono.when(saveNewTranslations, saveNewMuscleGroupAssociations))
                                        .then(Mono.defer(() -> {
                                            return findTranslationAndMuscleGroupsAndMapToDto(updatedExercise,
                                                    request.getTranslations().stream()
                                                            .map(TranslationRequest::getLanguageCode)
                                                            .findFirst()
                                                            .orElse("en")
                                            );
                                        }));
                            });
                })
                .switchIfEmpty(Mono.error(new RuntimeException("Exercise not found with id: " + id)));
    }

    private Mono<Void> saveExerciseTranslations(String exerciseId, List<TranslationRequest> translations) {
        return Flux.fromIterable(translations)
                .flatMap(translationRequest -> {
                    ExerciseTranslation translation = new ExerciseTranslation(
                            exerciseId,
                            translationRequest.getLanguageCode(),
                            translationRequest.getName(),
                            translationRequest.getDescription(),
                            translationRequest.getInstructions()
                    );
                    return exerciseTranslationRepository.save(translation);
                })
                .then();
    }

    private Mono<Void> saveExerciseMuscleGroupAssociations(String exerciseId, List<String> muscleGroupCodes) {
        if (muscleGroupCodes == null || muscleGroupCodes.isEmpty()) {
            return Mono.empty();
        }
        return muscleGroupService.getMuscleGroupIdsByCodes(muscleGroupCodes)
                .flatMapMany(muscleGroupIds ->
                        Flux.fromIterable(muscleGroupIds)
                                .map(muscleGroupId -> new ExerciseMuscleGroup(exerciseId, muscleGroupId))
                                .flatMap(exerciseMuscleGroupRepository::save)
                )
                .then();
    }

    private Mono<ExerciseDto> findTranslationAndMuscleGroupsAndMapToDto(Exercise exercise, String languageCode) {
        Mono<ExerciseTranslation> translationMono = exerciseTranslationRepository.findByExerciseIdAndLanguageCode(exercise.getId(), languageCode)
                .switchIfEmpty(Mono.defer(() -> {
                    if (!"en-GB".equals(languageCode)) {
                        return exerciseTranslationRepository.findByExerciseIdAndLanguageCode(exercise.getId(), "en-GB");
                    }
                    return Mono.empty();
                }))
                .defaultIfEmpty(new ExerciseTranslation(exercise.getId(), languageCode, "", "", ""));

        Mono<List<MuscleGroupDto>> muscleGroupsMono = exerciseMuscleGroupRepository.findMuscleGroupIdsByExerciseId(exercise.getId())
                .collect(Collectors.toSet())
                .flatMap(ids -> muscleGroupService.getMuscleGroupsByIdsAndLanguageCode(ids, languageCode))
                .map(responses -> responses.stream()
                        .map(MuscleGroupServiceResponse::getMuscleGroupDto)
                        .collect(Collectors.toList()));


        return Mono.zip(translationMono, muscleGroupsMono)
                .map(tuple -> toDto(exercise, tuple.getT1(), tuple.getT2()));
    }

    private ExerciseDto toDto(Exercise exercise, ExerciseTranslation translation, List<MuscleGroupDto> muscleGroups) {
        return new ExerciseDto(
                exercise.getId(),
                translation.getName(),
                translation.getDescription(),
                translation.getInstructions(),
                exercise.getMetValue(),
                muscleGroups
        );
    }
}