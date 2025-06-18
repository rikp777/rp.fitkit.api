package rp.fitkit.api.controller.advise;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import rp.fitkit.api.exception.ResourceNotFoundException;
import rp.fitkit.api.exception.UserAlreadyExistsException;

import java.net.URI;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
@AllArgsConstructor
@Order(-1)
public class GlobalExceptionHandler {
    private final MessageSource messageSource;

    private URI resolvePathFromServerWebExchange(ServerWebExchange exchange) {
        return exchange.getRequest().getURI();
    }

    private Locale resolveLocaleFromExchange(ServerWebExchange exchange) {
        return LocaleContextHolder.getLocale();
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ProblemDetail>> handleWebExchangeBindException(WebExchangeBindException ex, ServerWebExchange exchange) {
        List<ObjectError> allErrors = ex.getBindingResult().getAllErrors();
        Locale locale = resolveLocaleFromExchange(exchange);

        List<Map<String, String>> structuredErrors = allErrors.stream()
                .map(error -> {
                    String message = messageSource.getMessage(error, locale);
                    if (error instanceof FieldError fieldError) {
                        return Map.of(
                                "field", fieldError.getField(),
                                "message", message,
                                "code", fieldError.getCode() != null ? fieldError.getCode() : "VALIDATION_ERROR"
                        );
                    }
                    return Map.of(
                            "message", message,
                            "code", "VALIDATION_ERROR"
                    );
                })
                .toList();

        String detail = structuredErrors.stream()
                .map(err -> err.get("message"))
                .collect(Collectors.joining("\n"));

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
        problemDetail.setTitle(messageSource.getMessage("validation.error.title", null, locale));
        problemDetail.setInstance(resolvePathFromServerWebExchange(exchange));
        problemDetail.setProperty("timestamp", Instant.now().toString());
        problemDetail.setProperty("errors", structuredErrors);

        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail));
    }


    @ExceptionHandler(ResourceNotFoundException.class)
    public Mono<ResponseEntity<ProblemDetail>> handleResourceNotFoundException(ResourceNotFoundException ex, ServerWebExchange exchange) {
        Locale locale = resolveLocaleFromExchange(exchange);
        String detailMessage = messageSource.getMessage("error.resource.notfound", new Object[]{ex.getMessage()}, locale);
        String title = messageSource.getMessage("error.resource.notfound.title", null, locale);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, detailMessage);
        problemDetail.setTitle(title);
        problemDetail.setInstance(resolvePathFromServerWebExchange(exchange));
        problemDetail.setProperty("timestamp", LocalDateTime.now());
        problemDetail.setProperty("errorCode", "RESOURCE_NOT_FOUND");
        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(problemDetail));
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public Mono<ResponseEntity<ProblemDetail>> handleUserAlreadyExistsException(UserAlreadyExistsException ex, ServerWebExchange exchange) {
        Locale locale = resolveLocaleFromExchange(exchange);
        String detailMessage = messageSource.getMessage("error.user.alreadyexists", null, locale);
        String title = messageSource.getMessage("error.user.alreadyexists.title", null, locale);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, detailMessage);
        problemDetail.setTitle(title);
        problemDetail.setInstance(resolvePathFromServerWebExchange(exchange));
        problemDetail.setProperty("timestamp", LocalDateTime.now());
        problemDetail.setProperty("errorCode", "USER_ALREADY_EXISTS");
        return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).body(problemDetail));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public Mono<ResponseEntity<ProblemDetail>> handleBadCredentialsException(BadCredentialsException ex, ServerWebExchange exchange) {
        Locale locale = resolveLocaleFromExchange(exchange);
        String detailMessage = messageSource.getMessage("error.bad.credentials", null, locale);
        String title = messageSource.getMessage("error.authentication.failed.title", null, locale);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, detailMessage);
        problemDetail.setTitle(title);
        problemDetail.setInstance(resolvePathFromServerWebExchange(exchange));
        problemDetail.setProperty("timestamp", LocalDateTime.now());
        problemDetail.setProperty("errorCode", "BAD_CREDENTIALS");
        return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(problemDetail));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public Mono<ResponseEntity<ProblemDetail>> handleAccessDeniedException(AccessDeniedException ex, ServerWebExchange exchange) {
        Locale locale = resolveLocaleFromExchange(exchange);
        String detailMessage = messageSource.getMessage("error.access.denied", null, locale);
        String title = messageSource.getMessage("error.access.denied.title", null, locale);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, detailMessage);
        problemDetail.setTitle(title);
        problemDetail.setInstance(resolvePathFromServerWebExchange(exchange));
        problemDetail.setProperty("timestamp", LocalDateTime.now());
        problemDetail.setProperty("errorCode", "ACCESS_DENIED");
        return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).body(problemDetail));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public Mono<ResponseEntity<ProblemDetail>> handleDataIntegrityViolationException(DataIntegrityViolationException ex, ServerWebExchange exchange) {
        Locale locale = resolveLocaleFromExchange(exchange);
        log.debug("Advise heeft DataIntegrityViolationException opgevangen: " + ex.getMessage());
        String detailMessage = messageSource.getMessage("error.data.integrity.violation", null, locale);
        String title = messageSource.getMessage("error.data.conflict.title", null, locale);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, detailMessage);
        problemDetail.setTitle(title);
        problemDetail.setInstance(resolvePathFromServerWebExchange(exchange));
        problemDetail.setProperty("timestamp", LocalDateTime.now());
        problemDetail.setProperty("errorCode", "DATA_INTEGRITY_VIOLATION");
        return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).body(problemDetail));
    }

    @ExceptionHandler(NullPointerException.class)
    public Mono<ResponseEntity<ProblemDetail>> handleNullPointerException(NullPointerException ex, ServerWebExchange exchange) {
        Locale locale = resolveLocaleFromExchange(exchange);
        log.error("NullPointerException opgetreden, waarschijnlijk door een onverwachte null-waarde: ", ex);
        String detailMessage = messageSource.getMessage("error.unexpected", null, locale);
        String title = messageSource.getMessage("error.internal.server.error.title", null, locale);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, detailMessage);
        problemDetail.setTitle(title);
        problemDetail.setInstance(resolvePathFromServerWebExchange(exchange));
        problemDetail.setProperty("timestamp", LocalDateTime.now());
        problemDetail.setProperty("errorCode", "UNEXPECTED_ERROR");
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problemDetail));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ProblemDetail>> handleGenericException(Exception ex, ServerWebExchange exchange) {
        Locale locale = resolveLocaleFromExchange(exchange);
        log.error("Onverwachte fout opgetreden: ", ex);
        String detailMessage = messageSource.getMessage("error.unexpected", null, locale);
        String title = messageSource.getMessage("error.internal.server.error.title", null, locale);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, detailMessage);
        problemDetail.setTitle(title);
        problemDetail.setInstance(resolvePathFromServerWebExchange(exchange));
        problemDetail.setProperty("timestamp", LocalDateTime.now());
        problemDetail.setProperty("errorCode", "GENERIC_ERROR");
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problemDetail));
    }
}
