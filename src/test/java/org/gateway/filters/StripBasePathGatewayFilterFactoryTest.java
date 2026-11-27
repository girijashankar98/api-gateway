package org.gateway.filters;

import org.gateway.util.HeaderValidation;
import org.gateway.util.TokenUtil;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StripBasePathGatewayFilterFactoryTest {

    private static final String URL = "http://acme.example.com/apigateway/backend/x";

    private final HeaderValidation validation = mock(HeaderValidation.class);
    private final TokenUtil tokenUtil = mock(TokenUtil.class);
    private final GatewayFilterChain chain = mock(GatewayFilterChain.class);

    private MockServerWebExchange run(MockServerHttpRequest.BaseBuilder<?> req, String status) throws Exception {
        StripBasePathGatewayFilterFactory f = new StripBasePathGatewayFilterFactory("/apigateway");
        ReflectionTestUtils.setField(f, "headerValidation", validation);
        ReflectionTestUtils.setField(f, "tokenUtil", tokenUtil);
        when(validation.validateHeaders(any(), any())).thenReturn(status);
        when(tokenUtil.generateTokenForTenant("acme")).thenReturn("jwt");
        when(chain.filter(any())).thenReturn(Mono.empty());
        MockServerWebExchange ex = MockServerWebExchange.from(req);
        f.apply(new Object()).filter(ex, chain).block();
        return ex;
    }

    @Test
    void rejectsWhenOnlyOneHeaderPresent() throws Exception {
        MockServerWebExchange ex = run(MockServerHttpRequest.get(URL).header("key", "k"), "OK");
        assertEquals(HttpStatus.BAD_REQUEST, ex.getResponse().getStatusCode());
        verify(chain, never()).filter(any());
    }

    @Test
    void rejectsWrongCredentialsAndDoesNotForward() throws Exception {
        MockServerWebExchange ex = run(MockServerHttpRequest.get(URL)
                .header("key", "k").header("secret", "s").header("Authorization", "Bearer forged"), "UNAUTHORIZED");
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getResponse().getStatusCode());
        verify(chain, never()).filter(any());
    }

    @Test
    void forwardsWithTenantTokenWhenValid() throws Exception {
        run(MockServerHttpRequest.get(URL)
                .header("key", "k").header("secret", "s").header("Authorization", "Bearer forged"), "OK");
        ArgumentCaptor<ServerWebExchange> c = ArgumentCaptor.forClass(ServerWebExchange.class);
        verify(chain).filter(c.capture());
        assertEquals("Bearer jwt", c.getValue().getRequest().getHeaders().getFirst("Authorization"));
        assertNull(c.getValue().getRequest().getHeaders().getFirst("secret"));
        assertEquals("/backend/x", c.getValue().getRequest().getURI().getRawPath());
    }
}
