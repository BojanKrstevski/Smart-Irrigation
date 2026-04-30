package irrigation_system.service.impl;

import irrigation_system.model.Parcel;
import irrigation_system.model.Recommendation;
import irrigation_system.repository.ParcelRepository;
import irrigation_system.repository.RecommendationRepository;
import irrigation_system.service.RecommendationService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class RecommendationServiceImpl implements RecommendationService {

    private final ParcelRepository parcelRepository;
    private final RecommendationRepository recommendationRepository;

    public RecommendationServiceImpl(
            ParcelRepository parcelRepository,
            RecommendationRepository recommendationRepository
    ) {
        this.parcelRepository = parcelRepository;
        this.recommendationRepository = recommendationRepository;
    }

    @Override
    public String getDailyRecommendation(Long parcelId) {
        Parcel parcel = parcelRepository.findById(parcelId)
                .orElseThrow(() -> new IllegalArgumentException("Parcel not found with id: " + parcelId));

        double temperature = 30.0;
        double humidity = 35.0;
        boolean rainExpected = false;
        double waterAmount = calculateWaterAmount(parcel, temperature, humidity, rainExpected);
        boolean irrigationNeeded = waterAmount > 0;

        String explanation = buildExplanation(temperature, humidity, rainExpected, irrigationNeeded);

        Recommendation recommendation = new Recommendation();
        recommendation.setParcel(parcel);
        recommendation.setCreatedAt(LocalDate.now());
        recommendation.setIrrigationNeeded(irrigationNeeded);
        recommendation.setBestTime(irrigationNeeded ? "06:00 - 08:00" : "No irrigation needed");
        recommendation.setRecommendedWaterAmount(waterAmount);
        recommendation.setExplanation(explanation);
        recommendationRepository.save(recommendation);

        return explanation + " Recommended water amount: " + waterAmount + " liters.";
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

    private String buildExplanation(double temperature, double humidity, boolean rainExpected, boolean irrigationNeeded) {
        if (!irrigationNeeded) {
            return "Irrigation is not needed because humidity is high or rain is expected.";
        }

        return "Temperature is high, humidity is low, and no rain is expected in the next period, so irrigation is needed.";
    }
}
