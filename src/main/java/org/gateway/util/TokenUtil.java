package org.gateway.util;

import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@CacheConfig
public class TokenUtil
{
    Keycloak keycloak;
    @Value("${keycloak.clientId}")
    String kclientId;
    @Value("${keycloak.clientSecret}")
    String kclientSecret;
    @Value("${keycloak.url}")
    String kserverUrl;

    @Cacheable(cacheNames = "ApiGatewayToken", key = "#tenantId")
    public String generateTokenForTenant(String tenantId)
    {
        if (tenantId.equals(null))
        { return null; }


        keycloak = KeycloakBuilder.builder()
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .clientId(kclientId.trim())
                .clientSecret(kclientSecret.trim())
                .realm(tenantId)
                .serverUrl(kserverUrl.trim())
                .resteasyClient(new ResteasyClientBuilder().connectionPoolSize(10).build())
                .build();

        log.info("KeyCloak send Parameter {}",keycloak);
        String jwttoken = keycloak.tokenManager().grantToken().getToken();


        log.info(jwttoken);
        return jwttoken;
    }
}