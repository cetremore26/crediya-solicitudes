package co.com.crediya.security;

import co.com.crediya.model.auth.AuthenticatedUser;
import co.com.crediya.model.auth.Rol;
import co.com.crediya.model.auth.exception.InvalidTokenException;
import co.com.crediya.model.auth.gateways.TokenVerifier;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Date;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Verifies the JWTs issued by the authentication microservice. */
@Component
public class JwtTokenVerifier implements TokenVerifier {
    private static final Logger LOGGER = LoggerFactory.getLogger(JwtTokenVerifier.class);
    private static final String ISSUER = "crediya-authentication";
    private static final String CLAIM_EMAIL = "correo_electronico";
    private static final String CLAIM_ROL = "rol";
    private static final String CLAIM_DOCUMENTO = "documento_identidad";
    private static final int MINIMUM_SECRET_BYTES = 32;

    private final byte[] secret;

    public JwtTokenVerifier(@Value("${jwt.secret}") String secret) {
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        if (this.secret.length < MINIMUM_SECRET_BYTES) {
            throw new IllegalStateException(
                    "jwt.secret debe tener al menos " + MINIMUM_SECRET_BYTES + " caracteres.");
        }
    }

    @Override
    public AuthenticatedUser verify(String token) {
        try {
            SignedJWT jwt = SignedJWT.parse(token);
            if (!jwt.verify(new MACVerifier(secret))) {
                throw new InvalidTokenException("Firma del token inválida.");
            }
            JWTClaimsSet claims = jwt.getJWTClaimsSet();
            Date expiration = claims.getExpirationTime();
            if (!ISSUER.equals(claims.getIssuer())
                    || expiration == null
                    || expiration.before(new Date())) {
                throw new InvalidTokenException("Token expirado o de emisor desconocido.");
            }
            return new AuthenticatedUser(
                    UUID.fromString(claims.getSubject()),
                    claims.getStringClaim(CLAIM_EMAIL),
                    Rol.valueOf(claims.getStringClaim(CLAIM_ROL)),
                    claims.getStringClaim(CLAIM_DOCUMENTO));
        } catch (ParseException | JOSEException | IllegalArgumentException | NullPointerException exception) {
            LOGGER.warn("Token rechazado: {}", exception.getClass().getSimpleName());
            throw new InvalidTokenException("Token inválido.", exception);
        }
    }
}
