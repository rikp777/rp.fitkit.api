package rp.fitkit.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Set;

/**
 * Exception thrown when a client requests to sort by a field that is not supported or allowed.
 * <p>
 * This exception is automatically mapped to an HTTP 400 Bad Request response.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidSortFieldException extends RuntimeException {

    /**
     * Constructs a new InvalidSortFieldException with a detailed error message.
     *
     * @param invalidField  The unsupported sort field requested by the client.
     * @param allowedFields A set of the valid field names that are allowed for sorting.
     */
    public InvalidSortFieldException(String invalidField, Set<String> allowedFields) {
        super(String.format(
                "Invalid sort field '%s'. Allowed fields are: %s",
                invalidField,
                String.join(", ", allowedFields)
        ));
    }
}