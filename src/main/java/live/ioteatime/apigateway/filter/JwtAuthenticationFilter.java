package live.ioteatime.apigateway.filter;

import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import java.util.List;
import live.ioteatime.apigateway.exception.TokenException;
import live.ioteatime.apigateway.filter.JwtAuthenticationFilter.Config;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

@Component
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<Config> {

    @AllArgsConstructor
    @Getter @Setter
    public static class Config {

        private String secretKey;
    }

    public JwtAuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        String secretKey = config.getSecretKey();
        JwtParser jwtParser = getJwtParser(secretKey);

        return (exchange, chain) -> {
            List<String> authorizations = getAuthorizations(exchange);
            String jwt = getJwt(authorizations);
            String subject = getSubject(jwtParser, jwt);
            setXUserIdHeader(exchange, subject);

            return chain.filter(exchange);
        };
    }

    private void setXUserIdHeader(ServerWebExchange exchange, String subject) {
        exchange.mutate()
                .request(r -> r.header("X-USER-ID", subject)
                               .build());
    }

    private String getSubject(JwtParser jwtParser, String jwt) {

        return jwtParser.parseSignedClaims(jwt.substring(7))
                        .getPayload()
                        .getSubject();
    }

    private String getJwt(List<String> authorizations) {
        if (authorizations == null) {
            throw new TokenException("토큰을 찾을 수 없습니다.");
        }
        String jwt = authorizations.get(0);
        if (!jwt.startsWith("Bearer")) {
            throw new TokenException("잘못된 토큰 형식입니다.");
        }

        return jwt;
    }

    private List<String> getAuthorizations(ServerWebExchange exchange) {
        return exchange.getRequest()
                       .getHeaders()
                       .get(HttpHeaders.AUTHORIZATION);
    }

    private JwtParser getJwtParser(String secretKey) {

        return Jwts.parser()
                   .setSigningKey(secretKey)
                   .build();
    }
}
