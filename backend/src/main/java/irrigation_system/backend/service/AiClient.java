package irrigation_system.backend.service;

import irrigation_system.backend.dto.AiRecommendationRequest;
import irrigation_system.backend.dto.AiRecommendationResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class AiClient {

    private final RestClient restClient;

    public AiClient(RestClient aiRestClient) {
        this.restClient = aiRestClient;
    }

    public AiRecommendationResponse getRecommendation(AiRecommendationRequest request) {
        try {
            return restClient.post()
                    .uri("/ai/recommendation")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(AiRecommendationResponse.class);
        } catch (RestClientException e) {
            throw new IllegalStateException(
                    "AI service unavailable or returned error: " + e.getMessage(), e
            );
        }
    }
}
