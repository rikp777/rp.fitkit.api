package rp.fitkit.api.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import rp.fitkit.api.dto.ExerciseSessionResponseDto;
import rp.fitkit.api.dto.ProgressDataPointDto;
import rp.fitkit.api.model.SetLog;

import java.util.Comparator;
import java.util.Optional;

@Service
@Slf4j
public class StatisticsService {

    private final WorkoutSuggestionService workoutService;

    public StatisticsService(WorkoutSuggestionService workoutService) {
        this.workoutService = workoutService;
    }

    /**
     * Berekent de e1RM progressie voor een specifieke oefening van een gebruiker.
     * Deze versie is opgesplitst en gebruikt logging voor betere leesbaarheid en debugging.
     *
     * @param userId De ID van de gebruiker.
     * @param exerciseName De naam van de oefening.
     * @return Een Flux van datapunten, gesorteerd op datum.
     */
    @PreAuthorize("isAuthenticated()")
    public Flux<ProgressDataPointDto> getEstimated1rmHistory(String userId, String exerciseName) {
        log.info("Calculating e1RM history for user '{}' and exercise '{}'", userId, exerciseName);
        return workoutService.getHistoryForExercise(userId, exerciseName)
                .flatMap(this::createDataPointFromSession);
    }

    /**
     * Hulp-methode die een enkele trainingssessie analyseert en omzet naar een datapunt.
     *
     * @param session De trainingssessie DTO.
     * @return Een Mono met een ProgressDataPointDto als een top set is gevonden, anders een lege Mono.
     */
    private Mono<ProgressDataPointDto> createDataPointFromSession(ExerciseSessionResponseDto session) {
        if (session.getSets() == null || session.getSets().isEmpty()) {
            log.debug("Skipping session on {} because it has no sets.", session.getDate());
            return Mono.empty();
        }

        Optional<SetLog> topSetOpt = session.getSets().stream()
                .max(Comparator.comparingDouble(set -> calculateEpley1rm(set.getWeight(), set.getReps())));

        if (topSetOpt.isEmpty()) {
            log.debug("Could not determine a top set for session on {}.", session.getDate());
            return Mono.empty();
        }

        SetLog topSet = topSetOpt.get();
        double estimatedOneRepMax = calculateEpley1rm(topSet.getWeight(), topSet.getReps());

        log.debug("Calculated e1RM for session on {}: {:.2f} (from set: {} reps @ {} kg)",
                session.getDate(), estimatedOneRepMax, topSet.getReps(), topSet.getWeight());

        ProgressDataPointDto dataPoint = new ProgressDataPointDto(
                session.getDate(),
                estimatedOneRepMax,
                topSet.getWeight(),
                topSet.getReps()
        );

        return Mono.just(dataPoint);
    }

    /**
     * Berekent de e1RM met de Epley formule.
     */
    private double calculateEpley1rm(double weight, int reps) {
        if (reps == 1) return weight;
        return weight * (1 + (reps / 30.0));
    }
}
