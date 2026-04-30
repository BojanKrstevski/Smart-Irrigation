package irrigation_system.service;

import irrigation_system.model.Parcel;

public interface RecommendationService {

    String getDailyRecommendation(Long parcelId);

    double calculateWaterAmount(Parcel parcel, double temperature, double humidity, boolean rainExpected);
}