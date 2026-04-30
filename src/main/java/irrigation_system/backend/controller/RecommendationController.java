package irrigation_system.backend.controller;

import irrigation_system.backend.dto.DailyRecommendationResponse;
import irrigation_system.backend.dto.WaterAmountRequest;
import irrigation_system.backend.dto.WaterAmountResponse;
import irrigation_system.backend.model.Parcel;
import irrigation_system.backend.service.ParcelService;
import irrigation_system.backend.service.RecommendationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;
    private final ParcelService parcelService;

    public RecommendationController(RecommendationService recommendationService, ParcelService parcelService) {
        this.recommendationService = recommendationService;
        this.parcelService = parcelService;
    }

    @GetMapping("/parcel/{parcelId}/daily")
    public ResponseEntity<DailyRecommendationResponse> getDailyRecommendation(@PathVariable Long parcelId) {
        String recommendation = recommendationService.getDailyRecommendation(parcelId);
        return ResponseEntity.ok(new DailyRecommendationResponse(recommendation));
    }

    @PostMapping("/calculate")
    public ResponseEntity<WaterAmountResponse> calculateWaterAmount(@RequestBody WaterAmountRequest request) {
        Parcel parcel = parcelService.getById(request.parcelId());
        double waterAmount = recommendationService.calculateWaterAmount(
                parcel,
                request.temperature(),
                request.humidity(),
                request.rainExpected()
        );

        return ResponseEntity.ok(new WaterAmountResponse(waterAmount));
    }
}
