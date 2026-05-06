package irrigation_system.backend.service.impl;

import irrigation_system.backend.dto.WeatherResponse;
import irrigation_system.backend.model.Parcel;
import irrigation_system.backend.repository.ParcelRepository;
import irrigation_system.backend.service.WeatherService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class WeatherServiceImpl implements WeatherService {

    private final ParcelRepository parcelRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${weather.api.key}")
    private String apiKey;

    public WeatherServiceImpl(ParcelRepository parcelRepository) {
        this.parcelRepository = parcelRepository;
    }

    @Override
    public WeatherResponse getWeatherForParcel(Long parcelId) {
        Parcel parcel = parcelRepository.findById(parcelId)
                .orElseThrow(() -> new RuntimeException("Parcel not found"));

        String location = parcel.getLocation().split("-")[0].trim();

        String url = "https://api.openweathermap.org/data/2.5/weather?q="
                + location +
                "&appid=" + apiKey +
                "&units=metric";

        Map response = restTemplate.getForObject(url, Map.class);

        Map main = (Map) response.get("main");
        Map wind = (Map) response.get("wind");

        double temperature = ((Number) main.get("temp")).doubleValue();
        int humidity = ((Number) main.get("humidity")).intValue();
        double windSpeed = ((Number) wind.get("speed")).doubleValue();

        double rain = 0;

        if (response.containsKey("rain")) {
            Map rainMap = (Map) response.get("rain");

            if (rainMap != null && rainMap.containsKey("1h")) {
                rain = ((Number) rainMap.get("1h")).doubleValue();
            }
        }

        boolean irrigationNeeded = temperature > 28 && humidity < 45 && rain == 0;

        String reason;
        if (irrigationNeeded) {
            reason = "Температурата е висока, влажноста е ниска и нема дожд. Наводнување е потребно.";
        } else {
            reason = "Временските услови не покажуваат итна потреба за наводнување.";
        }

        return new WeatherResponse(
                temperature,
                humidity,
                windSpeed,
                rain,
                "Current weather for " + location,
                irrigationNeeded,
                reason
        );
    }
}