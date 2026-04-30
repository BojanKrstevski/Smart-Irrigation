package irrigation_system.dto;

import java.time.LocalDateTime;

public record ErrorResponse(String message, LocalDateTime timestamp) {
}
