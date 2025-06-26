package rp.fitkit.api.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import rp.fitkit.api.dto.ExerciseSessionResponseDto;
import rp.fitkit.api.dto.ExerciseTemplateDto;
import rp.fitkit.api.dto.WorkoutPlanDto;
import rp.fitkit.api.dto.WorkoutTemplateDto;
import rp.fitkit.api.exception.ResourceNotFoundException;
import rp.fitkit.api.model.ExerciseSession;
import rp.fitkit.api.model.ExerciseTemplate;
import rp.fitkit.api.model.WorkoutPlan;
import rp.fitkit.api.model.WorkoutTemplate;
import rp.fitkit.api.repository.*;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class WorkoutPlanService {

    private final WorkoutPlanRepository planRepository;
    private final WorkoutTemplateRepository templateRepository;
    private final ExerciseTemplateRepository exerciseRepository;

    @Autowired
    public WorkoutPlanService(
            WorkoutPlanRepository planRepository,
            WorkoutTemplateRepository templateRepository,
            ExerciseTemplateRepository exerciseRepository
    ) {
        this.planRepository = planRepository;
        this.templateRepository = templateRepository;
        this.exerciseRepository = exerciseRepository;
    }

    public Flux<WorkoutPlanDto> getPlansForUser(String userId) {
        log.info("Fetching all workout plans for user ID: {}", userId);
        return planRepository.findByUserId(userId)
                .flatMap(this::hydratePlan);
    }

    public Mono<WorkoutPlanDto> getPlanByIdAndUser(String planId, String userId) {
        log.info("Fetching workout plan with ID: {} for user ID: {}", planId, userId);
        return planRepository.findById(planId)
                .filter(plan -> plan.getUserId().equals(userId))
                .flatMap(this::hydratePlan);
    }

    @Transactional
    public Mono<WorkoutPlanDto> createWorkoutPlan(WorkoutPlanDto planDto, String userId) {
        log.info("Creating a new workout plan named '{}' for user '{}'", planDto.getName(), userId);

        WorkoutPlan plan = new WorkoutPlan();
        plan.setUserId(userId);
        plan.setName(planDto.getName());
        plan.setDescription(planDto.getDescription());
        plan.setActive(planDto.isActive());

        return planRepository.save(plan)
                .doOnSubscribe(subscription -> log.debug("Subscribed to save operation for plan '{}'", plan.getName()))
                .doOnError(error -> log.error("Error saving WorkoutPlan '{}'", plan.getName(), error))
                .doOnSuccess(savedPlan -> log.info("Successfully saved WorkoutPlan with ID: {}", savedPlan.getId()))
                .flatMap(savedPlan -> {
                    log.debug("Now saving templates for plan ID: {}", savedPlan.getId());
                    Flux<WorkoutTemplateDto> templatesFlux = Flux.fromIterable(planDto.getWorkoutTemplates());

                    return templatesFlux
                            .flatMap(templateDto -> createAndSaveTemplate(templateDto, savedPlan.getId()))
                            .collectList()
                            .map(savedTemplates -> {
                                log.info("All templates and exercises for plan ID: {} saved successfully.", savedPlan.getId());
                                return mapToPlanDto(savedPlan, savedTemplates);
                            });
                });
    }

    private Mono<WorkoutTemplateDto> createAndSaveTemplate(WorkoutTemplateDto templateDto, String planId) {
        WorkoutTemplate template = new WorkoutTemplate();
        template.setWorkoutPlanId(planId);
        template.setName(templateDto.getName());
        template.setDayOfWeek(templateDto.getDayOfWeek());

        return templateRepository.save(template)
                .flatMap(savedTemplate -> {
                    List<ExerciseTemplate> exercises = templateDto.getExercises().stream()
                            .map(exDto -> {
                                ExerciseTemplate ex = new ExerciseTemplate();
                                ex.setWorkoutTemplateId(savedTemplate.getId());
//                                ex.setExerciseName(exDto.getExerciseName());
//                                ex.setOrder(exDto.getOrder());
                                ex.setTargetSets(exDto.getTargetSets());
                                ex.setTargetRepsMin(exDto.getTargetRepsMin());
                                ex.setTargetRepsMax(exDto.getTargetRepsMax());
                                ex.setRestPeriodSeconds(exDto.getRestPeriodSeconds());
                                return ex;
                            }).collect(Collectors.toList());

                    return exerciseRepository.saveAll(exercises)
                            .collectList()
                            .map(savedExercises -> {
                                return mapToTemplateDto(savedTemplate, savedExercises);
                            });
                });
    }

    private WorkoutPlanDto mapToPlanDto(WorkoutPlan plan, List<WorkoutTemplateDto> templateDtos) {
        WorkoutPlanDto dto = new WorkoutPlanDto();
        dto.setId(plan.getId());
        dto.setUserId(plan.getUserId());
        dto.setName(plan.getName());
        dto.setDescription(plan.getDescription());
        dto.setActive(plan.isActive());
        dto.setWorkoutTemplates(templateDtos);
        return dto;
    }

    private WorkoutTemplateDto mapToTemplateDto(WorkoutTemplate template, List<ExerciseTemplate> exercises) {
        WorkoutTemplateDto dto = new WorkoutTemplateDto();
        dto.setId(template.getId());
        dto.setName(template.getName());
        dto.setDayOfWeek(template.getDayOfWeek());
        dto.setExercises(exercises.stream().map(this::mapToExerciseDto).collect(Collectors.toList()));
        return dto;
    }

    private ExerciseTemplateDto mapToExerciseDto(ExerciseTemplate exercise) {
        ExerciseTemplateDto dto = new ExerciseTemplateDto();
        dto.setId(exercise.getId());
//        dto.setExerciseName(exercise.getExerciseName());
//        dto.setOrder(exercise.getOrder());
        dto.setTargetSets(exercise.getTargetSets());
        dto.setTargetRepsMin(exercise.getTargetRepsMin());
        dto.setTargetRepsMax(exercise.getTargetRepsMax());
        dto.setRestPeriodSeconds(exercise.getRestPeriodSeconds());
        return dto;
    }

    private Mono<WorkoutPlanDto> hydratePlan(WorkoutPlan plan) {
        return templateRepository.findByWorkoutPlanId(plan.getId())
                .flatMap(template ->
                        exerciseRepository.findByWorkoutTemplateIdOrderByDisplayOrderAsc(template.getId())
                                .collectList()
                                .map(exercises -> mapToTemplateDto(template, exercises))
                )
                .collectList()
                .map(templateDtos -> mapToPlanDto(plan, templateDtos));
    }


    @Transactional
    public Mono<Void> deletePlan(String planId, String userId) {
        return planRepository.findById(planId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Plan met ID " + planId + " niet gevonden.")))
                .flatMap(plan -> {
                    if (!plan.getUserId().equals(userId)) {
                        return Mono.error(new AccessDeniedException("Gebruiker heeft geen toegang tot dit plan."));
                    }
                    return planRepository.delete(plan);
                });
    }
}
