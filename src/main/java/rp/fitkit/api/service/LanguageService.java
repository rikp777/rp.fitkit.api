package rp.fitkit.api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import rp.fitkit.api.dto.CreateLanguageRequest;
import rp.fitkit.api.dto.LanguageDto;
import rp.fitkit.api.model.Language;
import rp.fitkit.api.repository.LanguageRepository;

@Service
@RequiredArgsConstructor
public class LanguageService {

    private final LanguageRepository languageRepository;

    public Mono<LanguageDto> createLanguage(CreateLanguageRequest request) {
        Language language = new Language(request.getCode(), request.getName());
        return languageRepository.save(language)
                .map(this::toDto);
    }

    public Flux<LanguageDto> getAllLanguages() {
        return languageRepository.findAll()
                .map(this::toDto);
    }

    private LanguageDto toDto(Language language) {
        return new LanguageDto(language.getCode(), language.getName());
    }
}
