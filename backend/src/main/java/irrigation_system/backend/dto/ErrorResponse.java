package irrigation_system.backend.dto;

import java.time.LocalDateTime;

public record ErrorResponse(String message, LocalDateTime timestamp) {
}
