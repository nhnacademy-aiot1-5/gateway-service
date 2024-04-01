package live.ioteatime.gatewayservice.dto;

import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class ErrorResponse {

    private final LocalDateTime timestamp;
    private final String message;

    public ErrorResponse(String message) {
        timestamp = LocalDateTime.now();
        this.message = message;
    }
}
