package live.ioteatime.gatewayservice.handler;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest
@AutoConfigureWebTestClient
class ExceptionHandlerTest {

    @Autowired
    WebTestClient webTestClient;

    @Test
    @DisplayName("시그니처 예외 처리 테스트")
    void handleSignatureExceptionTest() {
        String secret = "b7299eb6fc972c6839647fcf8ae794fb9758b86ab14b9d8b5999c013cd095720231cd0408c41fd6f53f6d8b784082504dc1b57a24778f5cd1725488814c5fd47";
        String userId = "userId";
        String jwt = Jwts.builder()
                         .subject(userId)
                         .expiration(new Date(System.currentTimeMillis() + 60 * 1_000))
                         .signWith(SignatureAlgorithm.HS256, secret)
                         .compact();

        webTestClient.get().uri("/api")
                     .headers(httpHeaders -> httpHeaders.add(HttpHeaders.AUTHORIZATION, "Bearer " + jwt))
                     .exchange()
                     .expectStatus().isUnauthorized();
    }
}
