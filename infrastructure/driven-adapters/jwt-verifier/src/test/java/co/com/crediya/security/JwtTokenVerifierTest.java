package co.com.crediya.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import co.com.crediya.model.auth.AuthenticatedUser;
import co.com.crediya.model.auth.Rol;
import co.com.crediya.model.auth.exception.InvalidTokenException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class JwtTokenVerifierTest {
    private static final String SECRET = "una-clave-de-pruebas-de-al-menos-32-caracteres";

    private final JwtTokenVerifier verifier = new JwtTokenVerifier(SECRET);

    @Test
    void verifiesTokenIssuedByAuthenticationService() throws Exception {
        UUID id = UUID.randomUUID();

        AuthenticatedUser user = verifier.verify(token(SECRET, id, "crediya-authentication", 600));

        assertEquals(id, user.userId());
        assertEquals(Rol.CLIENTE, user.rol());
        assertEquals("1020304050", user.documentoIdentidad());
    }

    @Test
    void rejectsWrongSignatureExpiredAndForeignIssuer() throws Exception {
        String otherSecret = "otra-clave-de-pruebas-de-al-menos-32-caracteres";
        UUID id = UUID.randomUUID();

        assertThrows(InvalidTokenException.class,
                () -> verifier.verify(token(otherSecret, id, "crediya-authentication", 600)));
        assertThrows(InvalidTokenException.class,
                () -> verifier.verify(token(SECRET, id, "crediya-authentication", -10)));
        assertThrows(InvalidTokenException.class,
                () -> verifier.verify(token(SECRET, id, "otro-emisor", 600)));
        assertThrows(InvalidTokenException.class, () -> verifier.verify("basura"));
    }

    @Test
    void rejectsShortSecret() {
        assertThrows(IllegalStateException.class, () -> new JwtTokenVerifier("corta"));
    }

    private String token(String secret, UUID id, String issuer, long ttlSeconds) throws Exception {
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .issuer(issuer)
                .subject(id.toString())
                .claim("correo_electronico", "ana@example.com")
                .claim("rol", "CLIENTE")
                .claim("documento_identidad", "1020304050")
                .expirationTime(Date.from(Instant.now().plusSeconds(ttlSeconds)))
                .build();
        SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
        jwt.sign(new MACSigner(secret.getBytes(StandardCharsets.UTF_8)));
        return jwt.serialize();
    }
}
