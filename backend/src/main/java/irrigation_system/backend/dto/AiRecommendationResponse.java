package irrigation_system.backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AiRecommendationResponse(
        Long parcelId,
        boolean irrigationNeeded,
        String bestTime,
        double recommendedWaterAmount,
        String explanation,
        String explanationSource
) {
}
