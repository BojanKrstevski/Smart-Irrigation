package irrigation_system.backend.service;

import irrigation_system.backend.dto.WeatherResponse;

public interface WeatherService {
    WeatherResponse getWeatherForParcel(Long parcelId);
}