package org.gateway.util;

import lombok.extern.slf4j.Slf4j;
import org.gateway.tenant.TenanApiConfigRepo;
import org.gateway.tenant.TenantApiConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

@Service
@Slf4j
public class HeaderValidation {
    @Autowired
    TenanApiConfigRepo apiConfigRepo;
    private static final String KEY_REQUIRED_HEADER = "key";
    private static final String SECRET_REQUIRED_HEADER = "secret";

    public String validateHeaders(ServerWebExchange exchange, String tenant) throws NoSuchAlgorithmException {
        try {
            String key = exchange.getRequest().getHeaders().get(KEY_REQUIRED_HEADER).get(0);
            String secret = exchange.getRequest().getHeaders().get(SECRET_REQUIRED_HEADER).get(0);
            PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            if (passwordEncoder.matches(key, secret)) {
                return validateCredentials(tenant, secret);
            }
        }catch (Exception e){
            log.error(e.getLocalizedMessage());
            return "BAD REQUEST";
        }
        return "BAD REQUEST";
    }

        private String validateCredentials(String tenant,String secret) throws NoSuchAlgorithmException {
            log.debug("Key secret verified successfully");
            log.debug("finding configuration for tenant : " + tenant);
            Optional<TenantApiConfig> config = apiConfigRepo.findById(tenant);
            if (config.isPresent()) {
                log.debug("configuration found for tenant : " + tenant);
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                md.update("TenantAccountManager".getBytes());
                byte[] hashedSecret = md.digest(secret.getBytes(StandardCharsets.UTF_8));
                StringBuilder sb = new StringBuilder();
                for (byte b : hashedSecret) {
                    sb.append(String.format("%02x", b));
                }
                String hashString = sb.toString();
                log.debug("Generated hashcode to match : "+hashString +" and fetched hashcode is "+config.get().getHashSecret());
                if (hashString.equalsIgnoreCase(config.get().getHashSecret())) {
                    log.debug("Authentication successfully done");
                    return "OK";
                } else {
                    log.debug("Authentication failed");
                    return "UNAUTHORIZED";
                }
            }
            return "BAD REQUEST";
        }
}
