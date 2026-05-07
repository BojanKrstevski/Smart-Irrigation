package irrigation_system.backend.dto;

import java.time.LocalDate;
import java.util.List;

public record AiRecommendationRequest(
        Long parcelId,
        String name,
        String location,
        double size,
        String cropType,
        LocalDate lastIrrigation,
        List<IrrigationRecord> recentIrrigations
) {
    public record IrrigationRecord(LocalDate date, double waterAmount) {}
}
