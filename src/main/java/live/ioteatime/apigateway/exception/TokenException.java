package live.ioteatime.apigateway.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class TokenException extends RuntimeException {

    public TokenException(String message) {
        super(message);
    }
}
