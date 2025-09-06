package rp.fitkit.api.controller.mental;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import rp.fitkit.api.dto.mental.MentalHealthStepDto;
import rp.fitkit.api.model.user.User;
import rp.fitkit.api.service.mental.MentalHealthService;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@SpringBootTest
class MentalHealthControllerTest {

    @TestConfiguration
    static class TestSecurityConfig {
        @Bean
        @Primary
        public ReactiveUserDetailsService testUserDetailsService() {
            User mockUser = new User();
            mockUser.setId("user-123");
            mockUser.setUsername("testuser");
            mockUser.setPasswordHash("password");
            mockUser.setAuthoritiesFromRoles(List.of("ROLE_USER"));

            ReactiveUserDetailsService service = mock(ReactiveUserDetailsService.class);
            given(service.findByUsername("testuser")).willReturn(Mono.just(mockUser));
            return service;
        }
    }

    @Autowired
    private ApplicationContext context;

    private WebTestClient webTestClient;

    @MockBean
    private MentalHealthService mentalHealthService;

    private MentalHealthStepDto stepDto1;
    private MentalHealthStepDto stepDto2;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToApplicationContext(context).build();

        stepDto1 = new MentalHealthStepDto(1L, 1, "Mindfulness", "Breathe in, breathe out", "Relaxation", true, 1, 0);
        stepDto2 = new MentalHealthStepDto(2L, 2, "Gratitude", "What are you thankful for?", "Positivity", false, 3, 0);
    }

    @Test
    @DisplayName("Attempt to access endpoints without authentication should be unauthorized")
    void endpoints_WhenUnauthenticated_ShouldReturn401Unauthorized() {
        webTestClient.get().uri("/api/v1/mental-health/steps")
                .exchange()
                .expectStatus().isUnauthorized();

        webTestClient.get().uri("/api/v1/mental-health/steps/suggested")
                .exchange()
                .expectStatus().isUnauthorized();

        webTestClient.get().uri("/api/v1/mental-health/steps/1")
                .exchange()
                .expectStatus().isUnauthorized();

        webTestClient.post().uri("/api/v1/mental-health/steps/1/perform")
                .exchange()
                .expectStatus().isUnauthorized();
    }


    @Test
    @DisplayName("GET /steps with authenticated user should return steps")
    @WithUserDetails("testuser")
    void getMentalHealthSteps_AsUser_ShouldReturnSteps() {
        // Arrange
        given(mentalHealthService.getMentalHealthStepsForUser("user-123", "en-US")).willReturn(Flux.just(stepDto1, stepDto2));

        // Act & Assert
        webTestClient.get().uri("/api/v1/mental-health/steps?languageCode=en-US")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].id").isEqualTo(stepDto1.getId())
                .jsonPath("$[0].title").isEqualTo(stepDto1.getTitle())
                .jsonPath("$[1].id").isEqualTo(stepDto2.getId());
    }

    @Test
    @DisplayName("GET /steps/suggested with authenticated user should return a suggestion")
    @WithUserDetails("testuser")
    void getSuggestedMentalHealthStep_AsUser_ShouldReturnSuggestion() {
        // Arrange
        given(mentalHealthService.getSuggestedStepForUser("user-123", "en-US")).willReturn(Mono.just(stepDto1));

        // Act & Assert
        webTestClient.get().uri("/api/v1/mental-health/steps/suggested?languageCode=en-US")
                .exchange()
                .expectStatus().isOk()
                .expectBody(MentalHealthStepDto.class)
                .isEqualTo(stepDto1);
    }

    @Test
    @DisplayName("POST /steps/{stepId}/perform with authenticated user should return No Content")
    @WithUserDetails("testuser")
    void performStepAction_AsUser_ShouldReturnNoContent() {
        // Arrange
        given(mentalHealthService.performStepAction("user-123", 1L)).willReturn(Mono.empty());

        // Act & Assert
        webTestClient.post().uri("/api/v1/mental-health/steps/1/perform")
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    @DisplayName("GET /steps/suggested when no suggestion is available should return OK with empty body")
    @WithUserDetails("testuser")
    void getSuggestedMentalHealthStep_WhenNoSuggestion_ShouldReturnOkWithEmptyBody() {
        // Arrange
        given(mentalHealthService.getSuggestedStepForUser(anyString(), anyString())).willReturn(Mono.empty());

        // Act & Assert
        webTestClient.get().uri("/api/v1/mental-health/steps/suggested")
                .exchange()
                .expectStatus().isOk()
                .expectBody().isEmpty();
    }

    @Test
    @DisplayName("GET /steps/{stepId} with authenticated user should return a specific step")
    @WithUserDetails("testuser")
    void getMentalHealthStepById_AsUser_ShouldReturnStep() {
        // Arrange
        given(mentalHealthService.getMentalHealthStepForUser("user-123", 1L, "en-US")).willReturn(Mono.just(stepDto1));

        // Act & Assert
        webTestClient.get().uri("/api/v1/mental-health/steps/1?languageCode=en-US")
                .exchange()
                .expectStatus().isOk()
                .expectBody(MentalHealthStepDto.class)
                .isEqualTo(stepDto1);
    }
}