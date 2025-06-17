package rp.fitkit.api.controller.advise;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import rp.fitkit.api.exception.ResourceNotFoundException;
import rp.fitkit.api.exception.UserAlreadyExistsException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Vangt validatiefouten op die worden gegooid door de @Valid annotatie in reactieve controllers.
     * @param ex De WebExchangeBindException die alle details over de veldfouten bevat.
     * @return Een ResponseEntity met een 400 Bad Request status en een map van veldfouten.
     */
    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(WebExchangeBindException ex) {
        // Maak een Map om de fouten in op te slaan (bijv. "fieldName": "errorMessage")
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error -> {
            String fieldName = error.getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        // Geef een 400 Bad Request terug met de map van fouten in de body
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    /**
     * Vangt onze custom ResourceNotFoundException af.
     * @param ex De exception die is gegooid.
     * @return Een ResponseEntity met een 404 Not Found status en een foutbericht.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Vangt onze custom UserAlreadyExistsException af.
     * @param ex De exception die is gegooid.
     * @return Een ResponseEntity met een 409 Conflict status en een foutbericht.
     */
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handleUserAlreadyExistsException(UserAlreadyExistsException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    /**
     * Vangt de BadCredentialsException af die wordt gegooid bij een mislukte login.
     * @param ex De exception die is gegooid.
     * @return Een ResponseEntity met een 401 Unauthorized status en een duidelijke foutmelding.
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleBadCredentialsException(BadCredentialsException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Ongeldige gebruikersnaam of wachtwoord.");

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    /**
     * Vangt NullPointerExceptions af, die vaak optreden wanneer een @AuthenticationPrincipal
     * null is door een ongeldige sessie of token.
     * @param ex De NullPointerException.
     * @return Een 401 Unauthorized response om aan te geven dat de authenticatie is mislukt.
     */
    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<Map<String, String>> handleNullPointerException(NullPointerException ex) {
        log.error("NullPointerException opgetreden, waarschijnlijk door een mislukte authenticatie: ", ex);

        Map<String, String> error = new HashMap<>();
        error.put("error", "Authenticatie mislukt. Log opnieuw in.");

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }
}

