package irrigation_system.dto;

public record IrrigationScheduleResponse(
        Long id,
        Integer intervalDays,
        double waterAmount,
        boolean active,
        Long parcelId
) {
}
