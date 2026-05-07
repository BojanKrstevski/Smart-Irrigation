package irrigation_system.backend.service.impl;

import irrigation_system.backend.dto.AiRecommendationRequest;
import irrigation_system.backend.dto.AiRecommendationResponse;
import irrigation_system.backend.model.IrrigationHistory;
import irrigation_system.backend.model.Parcel;
import irrigation_system.backend.model.Recommendation;
import irrigation_system.backend.repository.IrrigationHistoryRepository;
import irrigation_system.backend.repository.ParcelRepository;
import irrigation_system.backend.repository.RecommendationRepository;
import irrigation_system.backend.service.AiClient;
import irrigation_system.backend.service.RecommendationService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class RecommendationServiceImpl implements RecommendationService {

    private static final int RECENT_IRRIGATION_DAYS = 7;

    private final ParcelRepository parcelRepository;
    private final RecommendationRepository recommendationRepository;
    private final IrrigationHistoryRepository irrigationHistoryRepository;
    private final AiClient aiClient;

    public RecommendationServiceImpl(
            ParcelRepository parcelRepository,
            RecommendationRepository recommendationRepository,
            IrrigationHistoryRepository irrigationHistoryRepository,
            AiClient aiClient
    ) {
        this.parcelRepository = parcelRepository;
        this.recommendationRepository = recommendationRepository;
        this.irrigationHistoryRepository = irrigationHistoryRepository;
        this.aiClient = aiClient;
    }

    @Override
    public String getDailyRecommendation(Long parcelId) {
        Parcel parcel = parcelRepository.findById(parcelId)
                .orElseThrow(() -> new IllegalArgumentException("Parcel not found with id: " + parcelId));

        AiRecommendationRequest request = buildRequest(parcel);
        AiRecommendationResponse aiResponse = aiClient.getRecommendation(request);

        Recommendation recommendation = new Recommendation();
        recommendation.setParcel(parcel);
        recommendation.setCreatedAt(LocalDate.now());
        recommendation.setIrrigationNeeded(aiResponse.irrigationNeeded());
        recommendation.setBestTime(aiResponse.bestTime());
        recommendation.setRecommendedWaterAmount(aiResponse.recommendedWaterAmount());
        recommendation.setExplanation(aiResponse.explanation());
        recommendationRepository.save(recommendation);

        if (aiResponse.irrigationNeeded()) {
            return aiResponse.explanation()
                    + " Recommended water amount: " + aiResponse.recommendedWaterAmount() + " liters."
                    + " Best time: " + aiResponse.bestTime() + ".";
        }
        return aiResponse.explanation();
    }

    @Override
    public double calculateWaterAmount(Parcel parcel, double temperature, double humidity, boolean rainExpected) {
        if (rainExpected || humidity >= 75.0) {
            return 0.0;
        }

        double baseAmount = parcel.getSize() * 40.0;

        if (temperature >= 30.0) {
            baseAmount *= 1.25;
        }

        if (humidity <= 40.0) {
            baseAmount *= 1.15;
        }

        return Math.round(baseAmount * 10.0) / 10.0;
    }

    private AiRecommendationRequest buildRequest(Parcel parcel) {
        LocalDate cutoff = LocalDate.now().minusDays(RECENT_IRRIGATION_DAYS);
        List<IrrigationHistory> history = Optional.ofNullable(
                irrigationHistoryRepository.findAllByParcel(parcel)
        ).orElse(List.of());

        List<AiRecommendationRequest.IrrigationRecord> recent = history.stream()
                .filter(h -> h.getIrrigationDate() != null && !h.getIrrigationDate().isBefore(cutoff))
                .sorted(Comparator.comparing(IrrigationHistory::getIrrigationDate).reversed())
                .map(h -> new AiRecommendationRequest.IrrigationRecord(
                        h.getIrrigationDate(), h.getWaterAmount()
                ))
                .toList();

        return new AiRecommendationRequest(
                parcel.getId(),
                parcel.getName(),
                parcel.getLocation(),
                parcel.getSize(),
                parcel.getCropType(),
                parcel.getLastIrrigation(),
                recent
        );
    }
}
