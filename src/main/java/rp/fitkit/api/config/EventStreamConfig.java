package rp.fitkit.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import rp.fitkit.api.dto.ExerciseSessionResponseDto;

@Configuration
public class EventStreamConfig {

    @Bean
    public Sinks.Many<ExerciseSessionResponseDto> workoutSessionSink() {
        return Sinks.many().multicast().onBackpressureBuffer();
    }

    @Bean
    public Flux<ExerciseSessionResponseDto> workoutSessionStream(Sinks.Many<ExerciseSessionResponseDto> sink) {
        return sink.asFlux();
    }
}
