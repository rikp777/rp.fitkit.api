package rp.fitkit.api.monitoring;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * {@link HealthIndicator} voor het monitoren van de gezondheid van een externe API-service.
 * Deze indicator controleert of een externe service (bijvoorbeeld een weer-API) bereikbaar is
 * en een verwachte respons geeft.
 */
@Component
public class ExternalApiServiceHealthIndicator implements HealthIndicator {

    private final WebClient webClient;

    public ExternalApiServiceHealthIndicator(WebClient.Builder webClientBuilder) {
        // Configureer de basis-URL van de externe service die je wilt monitoren.
        // VOORBEELD: "https://api.openweathermap.org/data/2.5/" als je OpenWeatherMap gebruikt
        this.webClient = webClientBuilder.baseUrl("https://api.open-meteo.com/").build();
    }

    /**
     * Voert de gezondheidscheck uit.
     * De methode probeert een lichtgewicht, snel endpoint van de externe service aan te roepen.
     * Een succesvolle respons duidt op 'UP', een fout of onverwachte respons op 'DOWN'.
     *
     * @return Een {@link Health} object dat de status (UP/DOWN) en eventuele details bevat.
     */
    @Override
    public Health health() {
        try {
            String statusResponse = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v1/forecast")
                            .queryParam("latitude", 52.52)
                            .queryParam("longitude", 13.41)
                            .queryParam("current_weather", true)
                            .build())
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError(), clientResponse ->
                            Mono.error(new RuntimeException("Open-Meteo client error: " + clientResponse.statusCode()))
                    )
                    .onStatus(status -> status.is5xxServerError(), clientResponse ->
                            Mono.error(new RuntimeException("Open-Meteo server error: " + clientResponse.statusCode()))
                    )
                    .bodyToMono(String.class)
                    .block(Duration.ofSeconds(5));

            if (statusResponse != null && statusResponse.contains("current_weather")) {
                return Health.up()
                        .withDetail("message", "Open-Meteo weather service is operationeel.")
                        .build();
            } else {
                return Health.down()
                        .withDetail("message", "Open-Meteo weer-service respons was onverwacht of onvolledig.")
                        .build();
            }
        } catch (Exception e) {
            return Health.down(e)
                    .withDetail("message", "Fout bij verbinding/verwerking met Open-Meteo weer-service: " + e.getMessage())
                    .build();
        }
    }
}
