package rp.fitkit.api.service.logbook;

import ai.djl.Application;
import ai.djl.inference.Predictor;
import ai.djl.modality.Classifications;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.training.util.ProgressBar;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import rp.fitkit.api.dto.logbook.LogSectionDto;
import rp.fitkit.api.dto.logbook.MoodStatsDto;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MoodAnalysisService {

    private ZooModel<String, Classifications> model;
    private Predictor<String, Classifications> predictor;

    private static final Pattern DIGITS = Pattern.compile("^\\d+$");

    @PostConstruct
    public void init() throws Exception {
        log.info("Loading sentiment model...");

        Criteria<String, Classifications> criteria = Criteria.<String, Classifications>builder()
                .optApplication(Application.NLP.TEXT_CLASSIFICATION)  // right app for sentiment
                .setTypes(String.class, Classifications.class)
                .optEngine("PyTorch")
                .optModelUrls("djl://ai.djl.huggingface.pytorch/distilbert-base-uncased-finetuned-sst-2-english")
                .optArgument("task", "text-classification")
                .optProgress(new ProgressBar())
                .build();

        this.model = criteria.loadModel();
        this.predictor = model.newPredictor();

        log.info("Sentiment model loaded.");
    }

    @PreDestroy
    public void destroy() {
        if (predictor != null) {
            predictor.close();
        }
        if (model != null) {
            model.close();
        }
        log.info("Sentiment analysis model closed.");
    }

    public MoodStatsDto calculateMoodStats(List<LogSectionDto> sections) {
        if (sections == null || sections.isEmpty()) {
            return new MoodStatsDto(0, 0, 0);
        }

        Map<String, Long> counts = sections.stream()
                .map(LogSectionDto::getMood)
                .filter(this::hasValue)
                .collect(Collectors.groupingBy(this::categorizeMood, Collectors.counting()));

        long positive = counts.getOrDefault("Positive", 0L);
        long neutral  = counts.getOrDefault("Neutral", 0L);
        long negative = counts.getOrDefault("Negative", 0L);

        log.debug("Final mood counts calculated: Positive={}, Neutral={}, Negative={}", positive, neutral, negative);

        return new MoodStatsDto((int) positive, (int) neutral, (int) negative);
    }

    private boolean hasValue(Object mood) {
        if (mood == null) return false;
        if (mood instanceof String s) return !s.isBlank();
        return true;
    }

    private String categorizeMood(Object mood) {
        log.debug("Categorizing mood input: '{}'", mood);

        try {
            if (mood instanceof Number n) {
                return starsToLabel(n.intValue());
            }

            String text = mood.toString().trim();
            if (text.isEmpty()) {
                return "Neutral";
            }

            if (DIGITS.matcher(text).matches()) {
                return starsToLabel(Integer.parseInt(text));
            }

            Classifications.Classification result = predictor.predict(text).best();
            String rawLabel = result.getClassName();

            String finalCategory = switch (rawLabel.toUpperCase()) {
                case "POSITIVE" -> "Positive";
                case "NEGATIVE" -> "Negative";
                default -> "Neutral";
            };

            log.debug("Model predicted raw label: '{}', Mapped to category: '{}'", rawLabel, finalCategory);
            return finalCategory;
        } catch (Exception e) {
            log.error("Failed to categorize mood: {}", mood, e);
            return "Neutral";
        }
    }

    private String starsToLabel(int stars) {
        log.debug("Mapping numeric mood rating: {}", stars);
        if (stars <= 0) return "Neutral";
        return switch (stars) {
            case 1, 2 -> "Negative";
            case 3 -> "Neutral";
            case 4, 5 -> "Positive";
            default -> "Positive";
        };
    }
}
