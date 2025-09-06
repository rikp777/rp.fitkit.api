//package rp.fitkit.api.seeder;
//
//import com.fasterxml.jackson.core.type.TypeReference;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
//import org.springframework.core.annotation.Order;
//import org.springframework.stereotype.Component;
//import reactor.core.publisher.Flux;
//import reactor.core.publisher.Mono;
//import rp.fitkit.api.dto.CreateExerciseRequest;
//import rp.fitkit.api.dto.ExerciseDto;
//import rp.fitkit.api.dto.MuscleGroupDto;
//import rp.fitkit.api.service.ExerciseService;
//
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Component
//@Order(3)
//@Slf4j
//@ConditionalOnProperty(
//    name = "app.seeding.enabled",
//    havingValue = "true",
//    matchIfMissing = true
//)
//public class ExerciseDataSeeder extends AbstractDataSeeder<CreateExerciseRequest, ExerciseDto> {
//
//    private final ExerciseService exerciseService;
//
//    public ExerciseDataSeeder(ObjectMapper objectMapper, ExerciseService exerciseService) {
//        super(objectMapper);
//        this.exerciseService = exerciseService;
//    }
//
//    @Override
//    protected String getFileName() {
//        return "exercises.json";
//    }
//
//
//    @Override
//    protected TypeReference<List<CreateExerciseRequest>> getTypeReference() {
//        return new TypeReference<List<CreateExerciseRequest>>() {};
//    }
//
//    @Override
//    protected Flux<ExerciseDto> fetchExistingData() {
//        return exerciseService.getAllExercises("en");
//    }
//
//    @Override
//    protected String getUniqueKey(CreateExerciseRequest data) {
//        String englishName = data.getTranslations().stream()
//                .filter(t -> "en".equals(t.getLanguageCode()))
//                .map(t -> t.getName().toLowerCase())
//                .findFirst()
//                .orElse("no_english_name");
//
//        List<String> sortedMuscleGroupCodes = data.getMuscleGroupCodes().stream()
//                .map(String::toLowerCase)
//                .sorted()
//                .collect(Collectors.toList());
//
//        return (englishName + "-" + String.join("_", sortedMuscleGroupCodes)).toLowerCase();
//    }
//
//    @Override
//    protected String getUniqueKeyFromExisting(ExerciseDto existingData) {
//        String exerciseName = existingData.getName().toLowerCase();
//
//        List<String> sortedMuscleGroupCodes = existingData.getMuscleGroups().stream()
//                .map(MuscleGroupDto::getCode)
//                .map(String::toLowerCase)
//                .sorted()
//                .collect(Collectors.toList());
//
//        return (exerciseName + "-" + String.join("_", sortedMuscleGroupCodes)).toLowerCase();
//    }
//
//    @Override
//    protected Mono<ExerciseDto> createEntity(CreateExerciseRequest request) {
//        return exerciseService.createExercise(request);
//    }
//}
