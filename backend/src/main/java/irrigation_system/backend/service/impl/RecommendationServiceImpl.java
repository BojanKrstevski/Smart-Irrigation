package irrigation_system.backend.service.impl;

import irrigation_system.backend.dto.WeatherResponse;
import irrigation_system.backend.model.Parcel;
import irrigation_system.backend.model.Recommendation;
import irrigation_system.backend.repository.ParcelRepository;
import irrigation_system.backend.repository.RecommendationRepository;
import irrigation_system.backend.service.RecommendationService;
import org.springframework.stereotype.Service;

import irrigation_system.backend.service.WeatherService;

import java.time.LocalDate;

@Service
public class RecommendationServiceImpl implements RecommendationService {

    private final ParcelRepository parcelRepository;
    private final RecommendationRepository recommendationRepository;
    private final WeatherService weatherService;

    public RecommendationServiceImpl(
            ParcelRepository parcelRepository,
            RecommendationRepository recommendationRepository,
            WeatherService weatherService) {
        this.parcelRepository = parcelRepository;
        this.recommendationRepository = recommendationRepository;
        this.weatherService = weatherService;
    }

    @Override
    public String getDailyRecommendation(Long parcelId) {
        Parcel parcel = parcelRepository.findById(parcelId)
                .orElseThrow(() -> new IllegalArgumentException("Parcel not found with id: " + parcelId));

        double temperature;
        double humidity;
        boolean rainExpected;

        try {
            WeatherResponse weather = weatherService.getWeatherForParcel(parcelId);

            temperature = weather.temperature();
            humidity = weather.humidity();
            rainExpected = weather.rain() > 0;

        } catch (Exception e) {
            System.out.println("Weather API failed, using fallback values");

            temperature = 30.0;
            humidity = 35.0;
            rainExpected = false;
        }

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
            return "Weather API data: temperature is " + temperature + "°C, humidity is " + humidity +
                    "%, rain expected: " + rainExpected + ". Irrigation is not needed.";
        }

        return "Weather API data: temperature is " + temperature + "°C, humidity is " + humidity +
                "%, rain expected: " + rainExpected + ". Irrigation is needed.";
    }
}
