package rp.fitkit.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class ConsentException extends RuntimeException {
    public ConsentException(String message) {
        super(message);
    }
}

