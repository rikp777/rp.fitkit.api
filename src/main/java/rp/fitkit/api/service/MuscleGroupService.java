package rp.fitkit.api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import rp.fitkit.api.dto.CreateMuscleGroupRequest;
import rp.fitkit.api.dto.MuscleGroupDto;
import rp.fitkit.api.dto.MuscleGroupServiceResponse;
import rp.fitkit.api.dto.MuscleGroupsResponse;
import rp.fitkit.api.model.muscleGroup.MuscleGroup;
import rp.fitkit.api.model.muscleGroup.MuscleGroupTranslation;
import rp.fitkit.api.repository.MuscleGroupRepository;
import rp.fitkit.api.repository.MuscleGroupTranslationRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MuscleGroupService {

    private final MuscleGroupRepository muscleGroupRepository;
    private final MuscleGroupTranslationRepository muscleGroupTranslationRepository;

    @Transactional
    public Mono<MuscleGroupDto> createMuscleGroup(CreateMuscleGroupRequest request) {
        MuscleGroup muscleGroup = new MuscleGroup(request.getCode(), request.getLatinName());

        return muscleGroupRepository.save(muscleGroup)
                .flatMap(savedMuscleGroup -> {
                    // Sla alle vertalingen op in één keer en verzamel ze
                    return Flux.fromIterable(request.getTranslations())
                            .map(translationRequest -> new MuscleGroupTranslation(
                                    savedMuscleGroup.getId(),
                                    translationRequest.getLanguageCode(),
                                    translationRequest.getName(),
                                    translationRequest.getDescription()
                            ))
                            .collectList()
                            .flatMapMany(muscleGroupTranslationRepository::saveAll)
                            .collectList()
                            .map(savedTranslations -> {
                                // Return English name if available, otherwise use the code
                                String name = savedTranslations.stream()
                                        .filter(t -> "en-GB".equals(t.getLanguageCode()))
                                        .map(MuscleGroupTranslation::getName)
                                        .findFirst()
                                        .orElse(request.getCode());
                                return toDto(savedMuscleGroup, name);
                            });
                });
    }

    public Flux<MuscleGroupServiceResponse> getAllMuscleGroups(String languageCode) {
        return muscleGroupRepository.findAll()
                .flatMap(muscleGroup ->
                        findTranslationForMuscleGroup(muscleGroup, languageCode)
                                .map(translation -> new MuscleGroupServiceResponse(toDto(muscleGroup, translation.getName()), translation.getLanguageCode()))
                                .switchIfEmpty(Mono.defer(() -> {
                                    return Mono.just(new MuscleGroupServiceResponse(toDto(muscleGroup, muscleGroup.getCode()), languageCode));
                                }))
                );
    }

    public Mono<MuscleGroupsResponse> getAllMuscleGroupsWithOmissions(String languageCode) {
        return muscleGroupRepository.findAll()
                .flatMap(muscleGroup ->
                        muscleGroupTranslationRepository.findByMuscleGroupIdAndLanguageCode(muscleGroup.getId(), languageCode)
                                .map(translation -> new TranslatedMuscleGroup(toDto(muscleGroup, translation.getName()), true))
                                .switchIfEmpty(Mono.just(new TranslatedMuscleGroup(toDto(muscleGroup, muscleGroup.getCode()), false))) // Als geen vertaling gevonden is, markeer als niet vertaald
                )
                .collectList()
                .map(translatedMuscleGroups -> {
                    List<MuscleGroupDto> muscleGroups = translatedMuscleGroups.stream()
                            .filter(TranslatedMuscleGroup::isTranslated)
                            .map(TranslatedMuscleGroup::getMuscleGroupDto)
                            .collect(Collectors.toList());

                    List<String> omittedMuscleGroupCodes = translatedMuscleGroups.stream()
                            .filter(t -> !t.isTranslated())
                            .map(t -> t.getMuscleGroupDto().getCode())
                            .collect(Collectors.toList());

                    return new MuscleGroupsResponse(muscleGroups, omittedMuscleGroupCodes);
                });
    }

    private static class TranslatedMuscleGroup {
        private final MuscleGroupDto muscleGroupDto;
        private final boolean translated;

        public TranslatedMuscleGroup(MuscleGroupDto muscleGroupDto, boolean translated) {
            this.muscleGroupDto = muscleGroupDto;
            this.translated = translated;
        }

        public MuscleGroupDto getMuscleGroupDto() {
            return muscleGroupDto;
        }

        public boolean isTranslated() {
            return translated;
        }
    }


    public Mono<Set<String>> getMuscleGroupIdsByCodes(List<String> muscleGroupCodes) {
        if (muscleGroupCodes == null || muscleGroupCodes.isEmpty()) {
            return Mono.just(Collections.emptySet());
        }
        return muscleGroupRepository.findByCodeIn(muscleGroupCodes)
                .map(MuscleGroup::getId)
                .collect(Collectors.toSet());
    }

    public Mono<MuscleGroupServiceResponse> getMuscleGroupByIdAndLanguageCode(String muscleGroupId, String languageCode) {
        return muscleGroupRepository.findById(muscleGroupId)
                .flatMap(muscleGroup ->
                        findTranslationForMuscleGroup(muscleGroup, languageCode)
                                .map(translation -> new MuscleGroupServiceResponse(toDto(muscleGroup, translation.getName()), translation.getLanguageCode()))
                                .switchIfEmpty(Mono.just(new MuscleGroupServiceResponse(toDto(muscleGroup, muscleGroup.getCode()), languageCode)))
                )
                .switchIfEmpty(Mono.error(new RuntimeException("Muscle group not found with id: " + muscleGroupId)));
    }

    public Mono<MuscleGroupServiceResponse> getMuscleGroupByCodeAndLanguageCode(String muscleGroupCode, String languageCode) {
        return muscleGroupRepository.findByCode(muscleGroupCode)
                .flatMap(muscleGroup ->
                        findTranslationForMuscleGroup(muscleGroup, languageCode)
                                .map(translation -> new MuscleGroupServiceResponse(toDto(muscleGroup, translation.getName()), translation.getLanguageCode()))
                                .switchIfEmpty(Mono.just(new MuscleGroupServiceResponse(toDto(muscleGroup, muscleGroup.getCode()), languageCode)))
                )
                .switchIfEmpty(Mono.error(new RuntimeException("Muscle group not found with code: " + muscleGroupCode)));
    }

    public Mono<List<MuscleGroupServiceResponse>> getMuscleGroupsByIdsAndLanguageCode(Set<String> muscleGroupIds, String languageCode) {
        if (muscleGroupIds.isEmpty()) {
            return Mono.just(Collections.emptyList());
        }
        return muscleGroupRepository.findAllById(muscleGroupIds)
                .flatMap(muscleGroup ->
                        findTranslationForMuscleGroup(muscleGroup, languageCode)
                                .map(translation -> new MuscleGroupServiceResponse(toDto(muscleGroup, translation.getName()), translation.getLanguageCode()))
                                .switchIfEmpty(Mono.just(new MuscleGroupServiceResponse(toDto(muscleGroup, muscleGroup.getCode()), languageCode)))
                )
                .collectList();
    }


    private MuscleGroupDto toDto(MuscleGroup muscleGroup, String name) {
        return new MuscleGroupDto(muscleGroup.getCode(), name, muscleGroup.getLatinName());
    }

    private Mono<MuscleGroupTranslation> findTranslationForMuscleGroup(MuscleGroup muscleGroup, String languageCode) {
        return muscleGroupTranslationRepository.findByMuscleGroupIdAndLanguageCode(muscleGroup.getId(), languageCode);
    }
}