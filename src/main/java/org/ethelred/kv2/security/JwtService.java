/* (C) Edward Harman and contributors 2022-2026 */
package org.ethelred.kv2.security;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import io.avaje.config.Config;
import jakarta.inject.Singleton;
import java.text.ParseException;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.ethelred.kv2.models.User;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class JwtService {
    private static final Logger LOGGER = LoggerFactory.getLogger(JwtService.class);
    private static final long EXPIRY_SECONDS = 7 * 24 * 60 * 60; // 7 days

    private final JWSSigner signer;
    private final JWSVerifier verifier;

    public JwtService() {
        var secret = Config.get("kv2.security.jwt-secret", "MTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTE=");
        try {
            var keyBytes = Base64.getDecoder().decode(secret);
            this.signer = new MACSigner(keyBytes);
            this.verifier = new MACVerifier(keyBytes);
        } catch (JOSEException e) {
            throw new IllegalStateException("Failed to initialise JwtService", e);
        }
    }

    public String generate(User user) {
        try {
            var now = Instant.now();
            var claims = new JWTClaimsSet.Builder()
                    .subject(user.id())
                    .issueTime(Date.from(now))
                    .expirationTime(Date.from(now.plusSeconds(EXPIRY_SECONDS)))
                    .claim("roles", user.roles())
                    .claim("displayName", user.displayName())
                    .claim("picture", user.pictureUrl())
                    .build();
            var jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
            jwt.sign(signer);
            return jwt.serialize();
        } catch (JOSEException e) {
            throw new RuntimeException("Failed to generate JWT", e);
        }
    }

    @Nullable
    public Principal parse(String token) {
        try {
            var jwt = SignedJWT.parse(token);
            if (!jwt.verify(verifier)) {
                return null;
            }
            var claims = jwt.getJWTClaimsSet();
            if (claims.getExpirationTime().before(new Date())) {
                return null;
            }
            @SuppressWarnings("unchecked")
            var roles = (List<String>) claims.getClaim("roles");
            var attributes = Map.<String, Object>of(
                    "displayName", orEmpty(claims.getStringClaim("displayName")),
                    "picture", orEmpty(claims.getStringClaim("picture")));
            return new Principal(claims.getSubject(), roles, attributes);
        } catch (ParseException | JOSEException e) {
            LOGGER.debug("Invalid JWT: {}", e.getMessage());
            return null;
        }
    }

    private String orEmpty(@Nullable String s) {
        return s != null ? s : "";
    }
}
