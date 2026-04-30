package irrigation_system.backend.dto;

import java.time.LocalDate;

public record ParcelRequest(
        String name,
        String location,
        double size,
        String cropType,
        LocalDate lastIrrigation,
        String notes
) {
}
