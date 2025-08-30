package rp.fitkit.api.controller.mental;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import rp.fitkit.api.dto.mental.request.CreateMentalHealthStepRequest;
import rp.fitkit.api.dto.mental.request.MentalHealthStepTranslationRequest;
import rp.fitkit.api.exception.ResourceNotFoundException;
import rp.fitkit.api.model.mental.MentalHealthStep;
import rp.fitkit.api.service.mental.AdminMentalHealthService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@SpringBootTest
class AdminMentalHealthControllerTest {

    @Autowired
    private ApplicationContext context;

    private WebTestClient webTestClient;

    @MockBean
    private AdminMentalHealthService adminMentalHealthService;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToApplicationContext(context).build();
    }

    @Test
    @DisplayName("Attempt to access admin endpoint without ADMIN role should be forbidden")
    @WithMockUser(roles = "USER")
    void adminEndpoints_WhenUserIsNotaAdmin_ShouldReturn403Forbidden() {
        webTestClient.get().uri("/api/v1/admin/mental-health/steps")
                .exchange()
                .expectStatus().isForbidden();

        webTestClient.post().uri("/api/v1/admin/mental-health/steps")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{}")
                .exchange()
                .expectStatus().isForbidden();

        webTestClient.delete().uri("/api/v1/admin/mental-health/steps/1")
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    @DisplayName("Attempt to access admin endpoint without authentication should be unauthorized")
    void adminEndpoints_WhenUnauthenticated_ShouldReturn401Unauthorized() {
        webTestClient.get().uri("/api/v1/admin/mental-health/steps")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("GET /steps as ADMIN should return all steps")
    @WithMockUser(roles = "ADMIN")
    void getAllMentalHealthSteps_AsAdmin_ShouldReturnAllSteps() {
        MentalHealthStep step1 = new MentalHealthStep();
        step1.setId(1L);
        step1.setStepNumber(1);

        given(adminMentalHealthService.getAllMentalHealthSteps()).willReturn(Flux.just(step1));

        webTestClient.get().uri("/api/v1/admin/mental-health/steps")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].id").isEqualTo(1)
                .jsonPath("$[0].stepNumber").isEqualTo(1);
    }

    @Test
    @DisplayName("POST /steps as ADMIN should create a new step")
    @WithMockUser(roles = "ADMIN")
    void createMentalHealthStep_AsAdmin_ShouldReturnCreatedStep() {
        CreateMentalHealthStepRequest request = new CreateMentalHealthStepRequest();
        request.setStepNumber(1);
        request.setRequiredCompletions(10);
        MentalHealthStepTranslationRequest translation = new MentalHealthStepTranslationRequest();
        translation.setLanguageCode("en-US");
        translation.setTitle("Test Title");
        request.setTranslations(List.of(translation));

        MentalHealthStep createdStep = new MentalHealthStep();
        createdStep.setId(1L);
        createdStep.setStepNumber(1);
        createdStep.setRequiredCompletions(10);

        given(adminMentalHealthService.createMentalHealthStep(any(CreateMentalHealthStepRequest.class)))
                .willReturn(Mono.just(createdStep));

        webTestClient.post().uri("/api/v1/admin/mental-health/steps")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(MentalHealthStep.class)
                .isEqualTo(createdStep);
    }

    @Test
    @DisplayName("POST /steps as ADMIN with invalid data should return Bad Request")
    @WithMockUser(roles = "ADMIN")
    void createMentalHealthStep_WithInvalidData_ShouldReturnBadRequest() {
        CreateMentalHealthStepRequest request = new CreateMentalHealthStepRequest();
        // Missing stepNumber, requiredCompletions, and translations to make it invalid

        webTestClient.post().uri("/api/v1/admin/mental-health/steps")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @DisplayName("DELETE /steps/{stepId} as ADMIN should return No Content")
    @WithMockUser(roles = "ADMIN")
    void deleteMentalHealthStep_AsAdmin_ShouldReturnNoContent() {
        given(adminMentalHealthService.deleteMentalHealthStep(1L)).willReturn(Mono.empty());

        webTestClient.delete().uri("/api/v1/admin/mental-health/steps/1")
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    @DisplayName("DELETE /steps/{stepId} as ADMIN for non-existing step should return Not Found")
    @WithMockUser(roles = "ADMIN")
    void deleteNonExistingMentalHealthStep_AsAdmin_ShouldReturnNotFound() {
        given(adminMentalHealthService.deleteMentalHealthStep(999L))
                .willReturn(Mono.error(new ResourceNotFoundException("Step not found")));

        webTestClient.delete().uri("/api/v1/admin/mental-health/steps/999")
                .exchange()
                .expectStatus().isNotFound();
    }
}