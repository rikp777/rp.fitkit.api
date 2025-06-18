package rp.fitkit.api.logging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class RateLimitingFilter implements WebFilter {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final Bandwidth limit = Bandwidth.classic(10, Refill.intervally(10, Duration.ofMinutes(1)));

    private final ObjectMapper objectMapper;
    private final MessageSource messageSource;

    // Constructor aanpassen voor injectie
    public RateLimitingFilter(ObjectMapper objectMapper, MessageSource messageSource) {
        this.objectMapper = objectMapper;
        this.messageSource = messageSource;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        if (path.equals("/api/v1/users/login") || path.equals("/api/v1/users/register")) {
            String ipAddress = getIpAddress(exchange);

            Bucket bucket = buckets.computeIfAbsent(ipAddress, k -> Bucket4j.builder().addLimit(limit).build());

            if (bucket.tryConsume(1)) {
                log.debug("Request from IP {} for path {} allowed. Tokens left: {}", ipAddress, path, bucket.getAvailableTokens());
                return chain.filter(exchange);
            } else {
                log.warn("Request from IP {} for path {} blocked due to rate limit.", ipAddress, path);
                return createTooManyRequestsResponse(exchange);
            }
        }

        return chain.filter(exchange);
    }

    private Mono<Void> createTooManyRequestsResponse(ServerWebExchange exchange) {
        Locale locale = LocaleContextHolder.getLocale(); // Haal de locale op

        // Haal gelokaliseerde titel en detail op
        String title = messageSource.getMessage("error.rate.limit.title", null, locale);
        String detail = messageSource.getMessage("error.rate.limit", null, locale);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.TOO_MANY_REQUESTS, detail);
        problemDetail.setTitle(title);
        problemDetail.setInstance(exchange.getRequest().getURI());
        problemDetail.setProperty("timestamp", LocalDateTime.now());
        problemDetail.setProperty("errorCode", "TOO_MANY_REQUESTS");

        exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_PROBLEM_JSON); // Belangrijk voor ProblemDetail

        try {
            byte[] bytes = objectMapper.writeValueAsBytes(problemDetail);
            return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(bytes)));
        } catch (JsonProcessingException e) {
            log.error("Error writing ProblemDetail for rate limit: {}", e.getMessage());
            // Fallback naar een simpele response als serialisatie faalt
            exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            exchange.getResponse().getHeaders().setContentType(MediaType.TEXT_PLAIN);
            return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap("Internal Server Error".getBytes())));
        }
    }

    private String getIpAddress(ServerWebExchange exchange) {
        if (exchange.getRequest().getHeaders().containsKey("X-Forwarded-For")) {
            return exchange.getRequest().getHeaders().get("X-Forwarded-For").get(0);
        }
        return exchange.getRequest().getRemoteAddress() != null ?
                exchange.getRequest().getRemoteAddress().getAddress().getHostAddress() : "unknown";
    }
}
