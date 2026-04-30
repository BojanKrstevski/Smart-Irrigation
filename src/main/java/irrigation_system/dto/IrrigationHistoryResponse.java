package irrigation_system.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record IrrigationHistoryResponse(
        Long id,
        LocalDate irrigationDate,
        LocalTime irrigationTime,
        double waterAmount,
        Long parcelId
) {
}
