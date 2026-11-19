package co.com.crediya.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import co.com.crediya.model.autenticacion.UsuarioAutenticado;
import co.com.crediya.model.autenticacion.Rol;
import co.com.crediya.model.autenticacion.exception.TokenInvalidoException;
import co.com.crediya.model.autenticacion.exception.VerificacionTokensNoDisponibleException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.KeySourceException;
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

class VerificadorTokensJwtTest {
    private static final RSAKey AUTH_KEY = generateKey();
    private static final RSAKey OTHER_KEY = generateKey();

    /** Verifica con la clave pública del servicio de autenticación, como haría con su JWKS. */
    private final VerificadorTokensJwt verificador =
            new VerificadorTokensJwt(new ImmutableJWKSet<>(new JWKSet(AUTH_KEY.toPublicJWK())));

    @Test
    void verifiesRs256TokenIssuedByAuthenticationService() throws Exception {
        UUID id = UUID.randomUUID();

        UsuarioAutenticado user = verificador.verificar(sign(AUTH_KEY, claims().subject(id.toString())));

        assertEquals(id, user.idUsuario());
        assertEquals(Rol.CLIENTE, user.rol());
        assertEquals("1020304050", user.documentoIdentidad());
    }

    @Test
    void acceptsStaffTokensWithoutDocument() throws Exception {
        UsuarioAutenticado user = verificador.verificar(sign(AUTH_KEY,
                claims().claim("rol", "ASESOR").claim("documento_identidad", null)));

        assertEquals(Rol.ASESOR, user.rol());
        assertNull(user.documentoIdentidad());
    }

    @Test
    void rejectsTokensSignedWithAnotherKeyOrWithHmac() throws Exception {
        String foreign = sign(OTHER_KEY, claims());
        SignedJWT hmac = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims().build());
        hmac.sign(new MACSigner("una-clave-hmac-de-al-menos-32-caracteres!!"));

        assertThrows(TokenInvalidoException.class, () -> verificador.verificar(foreign));
        assertThrows(TokenInvalidoException.class, () -> verificador.verificar(hmac.serialize()));
    }

    @Test
    void rejectsExpiredNotYetValidAndForeignIssuerTokens() throws Exception {
        Instant now = Instant.now();
        String expired = sign(AUTH_KEY, claims().expirationTime(Date.from(now.minusSeconds(600))));
        String notYetValid = sign(AUTH_KEY, claims().notBeforeTime(Date.from(now.plusSeconds(600))));
        String foreignIssuer = sign(AUTH_KEY, claims().issuer("otro-emisor"));

        assertThrows(TokenInvalidoException.class, () -> verificador.verificar(expired));
        assertThrows(TokenInvalidoException.class, () -> verificador.verificar(notYetValid));
        assertThrows(TokenInvalidoException.class, () -> verificador.verificar(foreignIssuer));
    }

    @Test
    void rejectsMissingOrInvalidClaimsAndGarbage() throws Exception {
        String withoutRole = sign(AUTH_KEY, claims().claim("rol", null));
        String unknownRole = sign(AUTH_KEY, claims().claim("rol", "SUPERUSUARIO"));
        String badSubject = sign(AUTH_KEY, claims().subject("no-es-uuid"));

        assertThrows(TokenInvalidoException.class, () -> verificador.verificar(withoutRole));
        assertThrows(TokenInvalidoException.class, () -> verificador.verificar(unknownRole));
        assertThrows(TokenInvalidoException.class, () -> verificador.verificar(badSubject));
        assertThrows(TokenInvalidoException.class, () -> verificador.verificar("no-es-un-jwt"));
    }

    @Test
    void reportsUnavailableKeysInsteadOfAnInvalidToken() throws Exception {
        VerificadorTokensJwt sinClaves = new VerificadorTokensJwt((selector, context) -> {
            throw new KeySourceException("JWKS no disponible");
        });
        String token = sign(AUTH_KEY, claims());

        assertThrows(VerificacionTokensNoDisponibleException.class, () -> sinClaves.verificar(token));
    }

    @Test
    void reportsUnavailableKeysWhenTheJwksEndpointDoesNotRespond() throws Exception {
        VerificadorTokensJwt sinServicio = new VerificadorTokensJwt("http://127.0.0.1:1/.well-known/jwks.json");
        String token = sign(AUTH_KEY, claims());

        assertThrows(VerificacionTokensNoDisponibleException.class, () -> sinServicio.verificar(token));
    }

    @Test
    void rejectsAnInvalidJwksUri() {
        assertThrows(IllegalStateException.class, () -> new VerificadorTokensJwt("no es una url"));
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
