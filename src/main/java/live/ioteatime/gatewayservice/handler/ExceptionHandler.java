package live.ioteatime.gatewayservice.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import live.ioteatime.gatewayservice.dto.ErrorResponse;
import live.ioteatime.gatewayservice.exception.TokenException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Slf4j
@RequiredArgsConstructor
public class ExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();
        response.getHeaders()
                .setContentType(MediaType.APPLICATION_JSON);

        if (ex instanceof SignatureException
            || ex instanceof MalformedJwtException
            || ex instanceof TokenException) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
        }
        String message = ex.getMessage();
        log.error(message);

        return response.writeWith(Mono.just(response.bufferFactory().wrap(serializeMessage(message))));
    }

    private byte[] serializeMessage(String message) {
        ErrorResponse errorResponse = new ErrorResponse(message);
        try {
            return objectMapper.writeValueAsBytes(errorResponse);
        } catch (JsonProcessingException e) {
            return "Error".getBytes();
        }
    }
}
