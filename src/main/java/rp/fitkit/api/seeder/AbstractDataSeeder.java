//package rp.fitkit.api.seeder;
//
//import com.fasterxml.jackson.core.type.TypeReference;
//import com.fasterxml.jackson.databind.JsonMappingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.boot.json.JsonParseException;
//import org.springframework.core.io.ClassPathResource;
//import reactor.core.publisher.Flux;
//import reactor.core.publisher.Mono;
//
//import java.io.FileNotFoundException;
//import java.io.IOException;
//import java.io.InputStream;
//import java.util.Collections;
//import java.util.List;
//import java.util.Set;
//import java.util.stream.Collectors;
//
//@RequiredArgsConstructor
//@Slf4j
//public abstract class AbstractDataSeeder<T, U> implements CommandLineRunner {
//
//    // ObjectMapper is required to deserialize JSON, injected by Spring
//    protected final ObjectMapper objectMapper;
//
//    /**
//     * Abstract method to be implemented by concrete seeders to specify
//     * the name of the JSON file to read (e.g., "exercises.json").
//     *
//     * @return The file name of the JSON data.
//     */
//    protected abstract String getFileName();
//
//    /**
//     * Abstract method to be implemented by concrete seeders to provide
//     * the TypeReference for Jackson to correctly deserialize the JSON list
//     * into the specific DTO type (T).
//     * Example: new TypeReference<List<CreateExerciseRequest>>() {}
//     *
//     * @return A TypeReference for the list of DTOs.
//     */
//    protected abstract TypeReference<List<T>> getTypeReference();
//
//    /**
//     * Abstract method to be implemented by concrete seeders to fetch
//     * all existing data (U type) from the database. This data will be
//     * used to determine if new items should be skipped.
//     *
//     * @return A Flux of existing DTOs (U).
//     */
//    protected abstract Flux<U> fetchExistingData();
//
//    /**
//     * Abstract method to be implemented by concrete seeders to define
//     * how to generate a unique key for a DTO (T) that is about to be seeded.
//     * This key is used to compare with existing data.
//     *
//     * @param data The DTO object (T) to generate a unique key for.
//     * @return A string representing the unique key for the DTO.
//     */
//    protected abstract String getUniqueKey(T data);
//
//    /**
//     * Abstract method to be implemented by concrete seeders to define
//     * how to generate a unique key for an already existing DTO (U).
//     * This key is used to compare with new data.
//     *
//     * @param existingData The existing DTO object (U) to generate a unique key for.
//     * @return A string representing the unique key for the existing DTO.
//     */
//    protected abstract String getUniqueKeyFromExisting(U existingData);
//
//    /**
//     * Abstract method to be implemented by concrete seeders to call the
//     * specific service method responsible for creating the entity from the DTO (T).
//     *
//     * @param request The DTO (T) used to create the entity.
//     * @return A Mono of the created entity's DTO (U).
//     */
//    protected abstract Mono<U> createEntity(T request);
//
//
//    /**
//     * The main run method that orchestrates the seeding process.
//     * It loads data from JSON, fetches existing data, filters out duplicates,
//     * and then inserts the new data into the database.
//     *
//     * @param args Command line arguments (not used here).
//     * @throws Exception if an error occurs during seeding.
//     */
//    @Override
//    public void run(String... args) throws Exception {
//        log.info("Starting Data Seeder for [{}]...", getFileName());
//
//        List<T> dataToSeed = loadDataFromJson("/seed/" + getFileName());
//
//        if (dataToSeed.isEmpty()) {
//            log.warn("No data loaded from JSON for [{}]. Skipping data seeding.", getFileName());
//            return;
//        }
//
//        Set<String> existingKeys = fetchExistingData()
//                .map(this::getUniqueKeyFromExisting)
//                .collect(Collectors.toSet())
//                .block();
//
//        if (existingKeys == null) {
//            existingKeys = Collections.emptySet();
//            log.warn("Could not retrieve existing data keys, proceeding with caution (potential duplicates).");
//        }
//
//        Set<String> finalExistingKeys = existingKeys;
//        Flux.fromIterable(dataToSeed)
//                .filter(item -> {
//                    String itemKey = getUniqueKey(item);
//                    boolean exists = finalExistingKeys.contains(itemKey);
//                    if (exists) {
//                        log.debug("Skipping existing item: [{}] in {}.", itemKey, getFileName());
//                    }
//                    return !exists;
//                })
//                .flatMap(this::createEntity)
//                .doOnNext(seededItem -> log.info("Successfully seeded item: [{}] from {}.", getUniqueKeyFromExisting(seededItem), getFileName()))
//                .doOnError(throwable -> log.error("Failed to seed item during reactive processing for {}: {}", getFileName(), throwable.getMessage(), throwable))
//                .doOnComplete(() -> log.info("Data Seeding Complete for [{}].", getFileName()))
//                .subscribe();
//    }
//
//    /**
//     * Helper method to load data from a JSON file.
//     *
//     * @param fileName The name of the JSON file (e.g., "data.json").
//     * @return A list of DTO objects (T), or an empty list if an error occurs.
//     */
//    private List<T> loadDataFromJson(String fileName) {
//        log.info("Attempting to load data from: {}", fileName);
//        try {
//            ClassPathResource resource = new ClassPathResource(fileName);
//            if (!resource.exists()) {
//                log.error("JSON file not found: {}. Please ensure it's in the classpath (e.g., src/main/resources).", fileName);
//                return Collections.emptyList();
//            }
//
//            try (InputStream inputStream = resource.getInputStream()) {
//                List<T> data = objectMapper.readValue(inputStream, getTypeReference());
//                log.info("Successfully loaded {} items from {}", data.size(), fileName);
//                return data;
//            }
//        } catch (FileNotFoundException e) {
//            log.error("JSON file '{}' was not found: {}", fileName, e.getMessage());
//        } catch (JsonParseException | JsonMappingException e) {
//            log.error("Error parsing JSON from '{}'. Please check file format and ensure it matches {}: {}", fileName, getTypeReference().getType(), e.getMessage(), e);
//        } catch (IOException e) {
//            log.error("General I/O error while reading '{}': {}", fileName, e.getMessage(), e);
//        }
//        return Collections.emptyList();
//    }
//}
