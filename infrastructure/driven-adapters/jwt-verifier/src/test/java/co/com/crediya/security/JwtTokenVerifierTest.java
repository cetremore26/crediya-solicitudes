package co.com.crediya.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import co.com.crediya.model.auth.AuthenticatedUser;
import co.com.crediya.model.auth.Rol;
import co.com.crediya.model.auth.exception.InvalidTokenException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class JwtTokenVerifierTest {
    private static final RSAKey AUTH_KEY = generateKey();
    private static final RSAKey OTHER_KEY = generateKey();

    /** Verifica con la clave pública del servicio de autenticación, como haría con su JWKS. */
    private final JwtTokenVerifier verifier =
            new JwtTokenVerifier(new ImmutableJWKSet<>(new JWKSet(AUTH_KEY.toPublicJWK())));

    @Test
    void verifiesRs256TokenIssuedByAuthenticationService() throws Exception {
        UUID id = UUID.randomUUID();

        AuthenticatedUser user = verifier.verify(sign(AUTH_KEY, claims().subject(id.toString())));

        assertEquals(id, user.userId());
        assertEquals(Rol.CLIENTE, user.rol());
        assertEquals("1020304050", user.documentoIdentidad());
    }

    @Test
    void acceptsStaffTokensWithoutDocument() throws Exception {
        AuthenticatedUser user = verifier.verify(sign(AUTH_KEY,
                claims().claim("rol", "ASESOR").claim("documento_identidad", null)));

        assertEquals(Rol.ASESOR, user.rol());
        assertNull(user.documentoIdentidad());
    }

    @Test
    void rejectsTokensSignedWithAnotherKeyOrWithHmac() throws Exception {
        String foreign = sign(OTHER_KEY, claims());
        SignedJWT hmac = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims().build());
        hmac.sign(new MACSigner("una-clave-hmac-de-al-menos-32-caracteres!!"));

        assertThrows(InvalidTokenException.class, () -> verifier.verify(foreign));
        assertThrows(InvalidTokenException.class, () -> verifier.verify(hmac.serialize()));
    }

    @Test
    void rejectsExpiredNotYetValidAndForeignIssuerTokens() throws Exception {
        Instant now = Instant.now();
        String expired = sign(AUTH_KEY, claims().expirationTime(Date.from(now.minusSeconds(600))));
        String notYetValid = sign(AUTH_KEY, claims().notBeforeTime(Date.from(now.plusSeconds(600))));
        String foreignIssuer = sign(AUTH_KEY, claims().issuer("otro-emisor"));

        assertThrows(InvalidTokenException.class, () -> verifier.verify(expired));
        assertThrows(InvalidTokenException.class, () -> verifier.verify(notYetValid));
        assertThrows(InvalidTokenException.class, () -> verifier.verify(foreignIssuer));
    }

    @Test
    void rejectsMissingOrInvalidClaimsAndGarbage() throws Exception {
        String withoutRole = sign(AUTH_KEY, claims().claim("rol", null));
        String unknownRole = sign(AUTH_KEY, claims().claim("rol", "SUPERUSUARIO"));
        String badSubject = sign(AUTH_KEY, claims().subject("no-es-uuid"));

        assertThrows(InvalidTokenException.class, () -> verifier.verify(withoutRole));
        assertThrows(InvalidTokenException.class, () -> verifier.verify(unknownRole));
        assertThrows(InvalidTokenException.class, () -> verifier.verify(badSubject));
        assertThrows(InvalidTokenException.class, () -> verifier.verify("no-es-un-jwt"));
    }

    @Test
    void rejectsAnInvalidJwksUri() {
        assertThrows(IllegalStateException.class, () -> new JwtTokenVerifier("no es una url"));
    }

    private static String sign(RSAKey key, JWTClaimsSet.Builder claims) throws Exception {
        SignedJWT jwt = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(key.getKeyID()).build(), claims.build());
        jwt.sign(new RSASSASigner(key));
        return jwt.serialize();
    }

    private static JWTClaimsSet.Builder claims() {
        Instant now = Instant.now();
        return new JWTClaimsSet.Builder()
                .issuer("crediya-authentication")
                .subject(UUID.randomUUID().toString())
                .claim("correo_electronico", "ana@example.com")
                .claim("rol", "CLIENTE")
                .claim("documento_identidad", "1020304050")
                .issueTime(Date.from(now))
                .notBeforeTime(Date.from(now))
                .expirationTime(Date.from(now.plusSeconds(600)));
    }

    private static RSAKey generateKey() {
        try {
            return new RSAKeyGenerator(2048).keyIDFromThumbprint(true).generate();
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }
}
