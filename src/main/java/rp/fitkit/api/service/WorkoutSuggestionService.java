package rp.fitkit.api.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import rp.fitkit.api.dto.ExerciseLogDto;
import rp.fitkit.api.model.ExerciseSession;
import rp.fitkit.api.model.SetLog;
import rp.fitkit.api.model.WorkoutSuggestion;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WorkoutSuggestionService {

    private final List<ExerciseSession> allUserSessions = new ArrayList<>();

    public WorkoutSuggestionService() {
        // Bestaande dummy data
        List<SetLog> sets1 = new ArrayList<>();
        sets1.add(new SetLog(10, 50));
        sets1.add(new SetLog(10, 50));
        allUserSessions.add(new ExerciseSession("user123", "Bench Press", LocalDate.now().minusDays(7), sets1, "Voelde goed"));

        List<SetLog> sets2 = new ArrayList<>();
        sets2.add(new SetLog(8, 60));
        sets2.add(new SetLog(8, 60));
        allUserSessions.add(new ExerciseSession("user123", "Squat", LocalDate.now().minusDays(5), sets2, "Zwaar!"));
    }

    // Je bestaande synchrone methode (kan blijven of later verwijderd worden)
    public Mono<WorkoutSuggestion> generateSuggestionReactive(String userId, String exerciseName) {
        return Flux.fromIterable(allUserSessions)
                .filter(session -> session.getUserId().equals(userId) &&
                        session.getExerciseName().equalsIgnoreCase(exerciseName))
                .sort(Comparator.comparing(ExerciseSession::getDate).reversed())
                .next()
                .map(lastSession -> {
                    List<SetLog> suggestedSets = new ArrayList<>();
                    String message = "Suggestie voor " + exerciseName + " gebaseerd op je training van " + lastSession.getDate() + ":";
                    for (SetLog lastSet : lastSession.getSets()) {
                        // SIMPEL ALGORITME (versie 0.1): +2.5kg, zelfde reps
                        suggestedSets.add(new SetLog(lastSet.getReps(), lastSet.getWeight() + 2.5));
                    }
                    return new WorkoutSuggestion(exerciseName, suggestedSets, message);
                })
                .defaultIfEmpty(createDefaultSuggestion(exerciseName)); // Als er geen sessies waren (Flux was leeg), geef een standaard suggestie
    }

    private WorkoutSuggestion createDefaultSuggestion(String exerciseName) {
        List<SetLog> suggestedSets = new ArrayList<>();
        suggestedSets.add(new SetLog(10, 20.0));
        suggestedSets.add(new SetLog(10, 20.0));
        suggestedSets.add(new SetLog(10, 20.0));
        String message = "Start met deze basis voor " + exerciseName + ". Log je training om betere suggesties te krijgen!";
        return new WorkoutSuggestion(exerciseName, suggestedSets, message);
    }

    public void logWorkoutSession(ExerciseSession session) {
        // TODO: Deze methode zou ook reactief kunnen worden als het opslaan (later naar DB) asynchroon gebeurt.
        // Voor nu voegen we het synchroon toe aan de lijst.
        allUserSessions.add(session);
        System.out.println("Workout gelogd: " + session + " (Huidig aantal sessies: " + allUserSessions.size() + ")");
    }

    /**
     * Logt een nieuwe workout sessie op een reactieve manier (voor nu in-memory).
     * @param userId De ID van de gebruiker die de workout logt.
     * @param logDto De DTO met de workout details.
     * @return Een Mono die de opgeslagen ExerciseSession bevat.
     */
    public Mono<ExerciseSession> logWorkoutSessionReactive(String userId, ExerciseLogDto logDto) {
        return Mono.fromCallable(() -> {
            ExerciseSession newSession = new ExerciseSession(); // Gebruikt de default constructor die ID en datum instelt
            newSession.setUserId(userId);
            newSession.setExerciseName(logDto.getExerciseName());
            // Gebruik de datum uit de DTO als die is meegegeven, anders de default van ExerciseSession
            if (logDto.getDate() != null) {
                newSession.setDate(logDto.getDate());
            }
            newSession.setSets(logDto.getSets() != null ? new ArrayList<>(logDto.getSets()) : new ArrayList<>());
            newSession.setNotes(logDto.getNotes());

            allUserSessions.add(newSession); // Voeg toe aan onze in-memory lijst
            System.out.println("REACTIEF Workout gelogd: " + newSession + " (Totaal: " + allUserSessions.size() + ")");
            return newSession; // Geef de aangemaakte sessie terug
        });
    }

}
