package rp.fitkit.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class InvalidDateRangeException extends ResponseStatusException {

    public InvalidDateRangeException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}