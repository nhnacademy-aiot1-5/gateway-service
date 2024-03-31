package live.ioteatime.gatewayservice.filter;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
import java.util.stream.Stream;
import live.ioteatime.gatewayservice.filter.JwtAuthenticationFilter.Config;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

class JwtAuthenticationFilterTest {

    JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {
        jwtAuthenticationFilter = new JwtAuthenticationFilter();
    }

    static Stream<Arguments> userIds() {

        return Stream.of(
            Arguments.of("test"),
            Arguments.of("userId"),
            Arguments.of("qrwe1234")
        );
    }

    @ParameterizedTest
    @MethodSource("userIds")
    @DisplayName("JWT 인증 필터 테스트")
    void jwtAuthenticationFilterTest(String expectedUserId) {
        final String secretKey = "795adf38949e2ea0cc18ec6e213b87e604cb0d6b773d516dc65898a8ed50207ad30e1188cc5b94f7eefb518dd321e0486dd64e1b2702d6333e63123d5e64b9e2";
        final String userIdHeader = "X-USER-ID";

        GatewayFilter gatewayFilter = getGatewayFilter(secretKey);
        ServerWebExchange exchange = getExchange(secretKey, expectedUserId);
        GatewayFilterChain chain = getGatewayFilterChain();

        gatewayFilter.filter(exchange, chain);
        String actualUserId = exchange.getRequest()
                                      .getHeaders()
                                      .get(userIdHeader)
                                      .get(0);

        Assertions.assertThat(actualUserId)
                  .isEqualTo(expectedUserId);
    }

    GatewayFilter getGatewayFilter(String secretKey) {
        Config config = new Config(secretKey);

        return jwtAuthenticationFilter.apply(config);
    }

    MockServerWebExchange getExchange(String secretKey, String expectedUserId) {
        String jwt = getJwt(secretKey, expectedUserId);
        MockServerHttpRequest request = MockServerHttpRequest.get("/")
                                                             .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                                                             .build();

        return MockServerWebExchange.from(request);
    }

    String getJwt(String secretKey, String expectedUserId) {
        final Date expiredDate = new Date(System.currentTimeMillis() + 60 * 1000);

        return Jwts.builder()
                   .signWith(SignatureAlgorithm.HS256, secretKey)
                   .subject(expectedUserId)
                   .expiration(expiredDate)
                   .compact();
    }

    GatewayFilterChain getGatewayFilterChain() {

        return exchange -> Mono.empty();
    }
}
