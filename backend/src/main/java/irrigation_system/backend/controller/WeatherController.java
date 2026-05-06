package irrigation_system.backend.controller;

import irrigation_system.backend.dto.WeatherResponse;
import irrigation_system.backend.service.WeatherService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/weather")
@CrossOrigin(origins = "*")
public class WeatherController {

    private final WeatherService weatherService;

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @GetMapping("/parcel/{parcelId}")
    public WeatherResponse getWeatherForParcel(@PathVariable Long parcelId) {
        return weatherService.getWeatherForParcel(parcelId);
    }
}