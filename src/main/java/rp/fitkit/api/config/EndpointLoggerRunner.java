package rp.fitkit.api.config;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.result.method.RequestMappingInfo;
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.pattern.PathPattern; // NIEUW: Importeer PathPattern
import rp.fitkit.api.logging.RateLimitingFilter;

import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Slf4j
public class EndpointLoggerRunner implements ApplicationRunner {

    private final RequestMappingHandlerMapping requestMappingHandlerMapping;
    private final RateLimitingFilter rateLimitingFilter;

    public EndpointLoggerRunner(RequestMappingHandlerMapping requestMappingHandlerMapping,
                                RateLimitingFilter rateLimitingFilter) {
        this.requestMappingHandlerMapping = requestMappingHandlerMapping;
        this.rateLimitingFilter = rateLimitingFilter;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.debug("=== API Endpoints Overview ===");

        Set<String> configuredRateLimitedPaths = rateLimitingFilter.getRateLimitedPaths();

        final int finalMaxStatusLength = "[RATE LIMITED]".length();

        final int finalMaxPathLength = requestMappingHandlerMapping.getHandlerMethods().entrySet().stream()
                .flatMap(entry -> entry.getKey().getPatternsCondition().getPatterns().stream())
                .mapToInt(pathPattern -> pathPattern.toString().length())
                .max()
                .orElse(0) + 5;

        log.debug(String.format(" %-" + finalMaxStatusLength + "s %-" + finalMaxPathLength + "s %s", "Status", "Endpoint", "Methode(s)"));
        log.debug(String.format(" %-" + finalMaxStatusLength + "s %-" + finalMaxPathLength + "s %s", "------", "--------", "-----------"));

        requestMappingHandlerMapping.getHandlerMethods().entrySet().stream()
                .sorted(Map.Entry.comparingByKey(Comparator.comparing(RequestMappingInfo::toString)))
                .forEach(entry -> {
                    RequestMappingInfo mappingInfo = entry.getKey();
                    Set<String> patterns = mappingInfo.getPatternsCondition().getPatterns().stream()
                            .map(PathPattern::toString)
                            .collect(Collectors.toSet());

                    patterns.forEach(pattern -> {
                        boolean hasRateLimiter = configuredRateLimitedPaths.contains(pattern) ||
                                configuredRateLimitedPaths.stream().anyMatch(rp -> pattern.matches(rp.replace("**", ".*")));

                        String status = hasRateLimiter ? "[RATE LIMITED]" : "[OPEN]";
                        String method = mappingInfo.getMethodsCondition().getMethods().toString();

                        log.debug(String.format(" %-" + finalMaxStatusLength + "s %-" + finalMaxPathLength + "s %s", status, pattern, method));
                    });
                });
        log.debug("=== Endpoints Overview Complete ===");
    }
}

