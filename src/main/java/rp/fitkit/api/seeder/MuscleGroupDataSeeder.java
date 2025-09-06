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
//import rp.fitkit.api.dto.CreateMuscleGroupRequest;
//import rp.fitkit.api.dto.MuscleGroupDto;
//import rp.fitkit.api.dto.MuscleGroupServiceResponse;
//import rp.fitkit.api.dto.TranslationRequest;
//import rp.fitkit.api.service.MuscleGroupService;
//
//import java.util.List;
//
//@Component
//@Order(2)
//@Slf4j
//@ConditionalOnProperty(
//    name = "app.seeding.enabled",
//    havingValue = "true",
//    matchIfMissing = true
//)
//public class MuscleGroupDataSeeder extends AbstractDataSeeder<CreateMuscleGroupRequest, MuscleGroupDto> {
//
//    private final MuscleGroupService muscleGroupService;
//
//    public MuscleGroupDataSeeder(ObjectMapper objectMapper, MuscleGroupService muscleGroupService) {
//        super(objectMapper);
//        this.muscleGroupService = muscleGroupService;
//    }
//
//    @Override
//    protected String getFileName() {
//        return "muscle_groups.json";
//    }
//
//    @Override
//    protected TypeReference<List<CreateMuscleGroupRequest>> getTypeReference() {
//        return new TypeReference<List<CreateMuscleGroupRequest>>() {};
//    }
//
//    @Override
//    protected Flux<MuscleGroupDto> fetchExistingData() {
//        return  muscleGroupService.getAllMuscleGroups("en-GB")
//                .map(MuscleGroupServiceResponse::getMuscleGroupDto);
//    }
//
//    @Override
//    protected String getUniqueKey(CreateMuscleGroupRequest data) {
//        return data.getCode().toLowerCase();
//    }
//
//    @Override
//    protected String getUniqueKeyFromExisting(MuscleGroupDto existingData) {
//        return existingData.getCode().toLowerCase();
//    }
//
//    @Override
//    protected Mono<MuscleGroupDto> createEntity(CreateMuscleGroupRequest request) {
//        return muscleGroupService.createMuscleGroup(request);
//    }
//}
