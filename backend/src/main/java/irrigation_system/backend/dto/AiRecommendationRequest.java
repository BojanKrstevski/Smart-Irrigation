package irrigation_system.backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.util.List;

public record AiRecommendationRequest(
        Long parcelId,
        String name,
        String location,
        double size,
        String cropType,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        LocalDate lastIrrigation,
        List<IrrigationRecord> recentIrrigations
) {
    public record IrrigationRecord(
            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
            LocalDate date,
            double waterAmount
    ) {}
}
