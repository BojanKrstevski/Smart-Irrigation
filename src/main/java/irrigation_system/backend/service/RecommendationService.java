package irrigation_system.backend.service;

import irrigation_system.backend.model.Parcel;

public interface RecommendationService {

    String getDailyRecommendation(Long parcelId);

    double calculateWaterAmount(Parcel parcel, double temperature, double humidity, boolean rainExpected);
}