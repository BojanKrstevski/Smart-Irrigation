package irrigation_system.backend.dto;

public record WeatherResponse(
        double temperature,
        int humidity,
        double windSpeed,
        double rain,
        String description,
        boolean irrigationNeeded,
        String reason
) {
}