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
//import rp.fitkit.api.dto.CreateLanguageRequest;
//import rp.fitkit.api.dto.LanguageDto;
//import rp.fitkit.api.service.LanguageService;
//
//import java.util.List;
//
//@Component
//@Order(1)
//@Slf4j
//@ConditionalOnProperty(
//    name = "app.seeding.enabled",
//    havingValue = "true",
//    matchIfMissing = true
//)
//public class LanguageDataSeeder extends AbstractDataSeeder<CreateLanguageRequest, LanguageDto> {
//
//    private final LanguageService languageService;
//
//    public LanguageDataSeeder(ObjectMapper objectMapper, LanguageService languageService) {
//        super(objectMapper);
//        this.languageService = languageService;
//    }
//    @Override
//    protected String getFileName() {
//        return "languages.json";
//    }
//
//    @Override
//    protected TypeReference<List<CreateLanguageRequest>> getTypeReference() {
//        return new TypeReference<List<CreateLanguageRequest>>() {};
//    }
//
//    @Override
//    protected Flux<LanguageDto> fetchExistingData() {
//        return languageService.getAllLanguages();
//    }
//
//    @Override
//    protected String getUniqueKey(CreateLanguageRequest data) {
//        return data.getCode().toLowerCase();
//    }
//
//    @Override
//    protected String getUniqueKeyFromExisting(LanguageDto existingData) {
//        return existingData.getCode().toLowerCase();
//    }
//
//    @Override
//    protected Mono<LanguageDto> createEntity(CreateLanguageRequest request) {
//        return languageService.createLanguage(request);
//    }
//}
//
