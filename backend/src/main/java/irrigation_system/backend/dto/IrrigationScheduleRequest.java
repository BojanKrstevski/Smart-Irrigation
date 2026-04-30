package irrigation_system.backend.dto;

public record IrrigationScheduleRequest(Integer intervalDays, double waterAmount, boolean active) {
}
