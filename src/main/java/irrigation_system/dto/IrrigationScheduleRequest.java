package irrigation_system.dto;

public record IrrigationScheduleRequest(Integer intervalDays, double waterAmount, boolean active) {
}
