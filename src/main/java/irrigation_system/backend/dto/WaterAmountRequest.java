package irrigation_system.backend.dto;

public record WaterAmountRequest(Long parcelId, double temperature, double humidity, boolean rainExpected) {
}
