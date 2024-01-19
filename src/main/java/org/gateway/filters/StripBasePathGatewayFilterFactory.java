package org.gateway.filters;

import lombok.extern.slf4j.Slf4j;
import org.gateway.util.HeaderValidation;
import org.gateway.util.TokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import java.security.NoSuchAlgorithmException;

@Component
@Slf4j
public class StripBasePathGatewayFilterFactory extends AbstractGatewayFilterFactory<Object> {

    private final String contextPath;
    @Autowired
    TokenUtil tokenUtil;
    @Autowired
    HeaderValidation headerValidation;
    private static final String KEY_REQUIRED_HEADER = "key";
    private static final String SECRET_REQUIRED_HEADER = "secret";
    private static final String TOKEN_PREFIX = "Bearer ";

    StripBasePathGatewayFilterFactory(@Value("${spring.webflux.base-path}") String contextPath) {
        this.contextPath = contextPath;
    }
    @Override
    public GatewayFilter apply(Object config) {
        return (exchange, chain) -> {

            ServerHttpRequest request = exchange.getRequest();
            String[] hostDomain = UriComponentsBuilder.fromUri(exchange.getRequest().getURI()).build().getHost().split("\\.");

            if (!request.getHeaders().containsKey(KEY_REQUIRED_HEADER) && !request.getHeaders().containsKey(SECRET_REQUIRED_HEADER)) {
                log.debug("Requested Headers is missing");
                exchange.getResponse().setStatusCode(HttpStatus.BAD_REQUEST);
                return exchange.getResponse().setComplete();
            }
            String validationStatus = null;
            try {
                validationStatus = headerValidation.validateHeaders(exchange,hostDomain[0]);
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
            if (validationStatus.equals("OK")){
                ServerHttpRequest modifiedRequest = request.mutate()
                        .header(HttpHeaders.AUTHORIZATION, TOKEN_PREFIX + tokenUtil.generateTokenForTenant(hostDomain[0]))
                        .build();
                exchange = exchange.mutate().request(modifiedRequest).build();
            }
            return chain.filter(exchange.mutate()
                    .request(exchange.getRequest().mutate()
                            .path(exchange.getRequest().getURI().getRawPath().replaceFirst(this.contextPath, ""))
                            .contextPath(null).build())
                    .build());
        };
    }

}
