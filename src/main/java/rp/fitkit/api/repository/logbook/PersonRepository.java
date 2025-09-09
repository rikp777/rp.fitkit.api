package rp.fitkit.api.repository.logbook;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import rp.fitkit.api.model.logbook.Person;
import rp.fitkit.api.model.root.Visibility;

@Repository
public interface PersonRepository extends R2dbcRepository<Person, String> {
    /**
     * Finds all persons that are either owned by a specific user OR have a specific visibility level (e.g., GLOBAL).
     * This is the core query to find all persons a user is allowed to see.
     *
     * @param userId The ID of the user who owns the person.
     * @param visibility The visibility level to check for (typically GLOBAL).
     * @return A Flux of persons matching the criteria.
     */
    Flux<Person> findByUserIdOrVisibility(String userId, Visibility visibility);
}

