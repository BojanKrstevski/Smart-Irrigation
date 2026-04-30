package irrigation_system.dto;

public record WaterAmountRequest(Long parcelId, double temperature, double humidity, boolean rainExpected) {
}
