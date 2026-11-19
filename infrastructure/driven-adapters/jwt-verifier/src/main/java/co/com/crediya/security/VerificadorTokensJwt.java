package co.com.crediya.security;

import co.com.crediya.model.autenticacion.UsuarioAutenticado;
import co.com.crediya.model.autenticacion.Rol;
import co.com.crediya.model.autenticacion.exception.TokenInvalidoException;
import co.com.crediya.model.autenticacion.gateways.VerificadorTokens;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.JWKSourceBuilder;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jose.util.DefaultResourceRetriever;
import com.nimbusds.jwt.JWTClaimNames;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import java.net.MalformedURLException;
import java.net.URI;
import java.text.ParseException;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Verifica los JWT emitidos por el microservicio de autenticación.
 *
 * <p>Los tokens se firman con RS256 y aquí solo se verifican con la clave pública publicada en
 * {@code jwt.jwks-uri}: este servicio no tiene ningún secreto con el que pudiera emitirlos.
 * Las claves se descargan la primera vez que se necesitan y se guardan en caché.
 */
@Component
public class VerificadorTokensJwt implements VerificadorTokens {
    private static final Logger LOGGER = LoggerFactory.getLogger(VerificadorTokensJwt.class);
    private static final String EMISOR = "crediya-authentication";
    private static final String CLAIM_CORREO = "correo_electronico";
    private static final String CLAIM_ROL = "rol";
    private static final String CLAIM_DOCUMENTO = "documento_identidad";
    private static final Set<String> CLAIMS_OBLIGATORIOS = Set.of(
            JWTClaimNames.SUBJECT, JWTClaimNames.EXPIRATION_TIME, JWTClaimNames.ISSUED_AT,
            CLAIM_CORREO, CLAIM_ROL);
    private static final int TIEMPO_ESPERA_JWKS_MS = 3000;
    private static final int LIMITE_BYTES_JWKS = 50 * 1024;

    private final ConfigurableJWTProcessor<SecurityContext> processor = new DefaultJWTProcessor<>();

    @Autowired
    public VerificadorTokensJwt(@Value("${jwt.jwks-uri}") String jwksUri) {
        this(clavesRemotas(jwksUri));
    }

    VerificadorTokensJwt(JWKSource<SecurityContext> keys) {
        processor.setJWSKeySelector(new JWSVerificationKeySelector<>(JWSAlgorithm.RS256, keys));
        processor.setJWTClaimsSetVerifier(new DefaultJWTClaimsVerifier<>(
                new JWTClaimsSet.Builder().issuer(EMISOR).build(), CLAIMS_OBLIGATORIOS));
    }

    @Override
    public UsuarioAutenticado verificar(String token) {
        try {
            JWTClaimsSet claims = processor.process(token, null);
            return new UsuarioAutenticado(
                    UUID.fromString(claims.getSubject()),
                    claims.getStringClaim(CLAIM_CORREO),
                    Rol.valueOf(claims.getStringClaim(CLAIM_ROL)),
                    claims.getStringClaim(CLAIM_DOCUMENTO));
        } catch (ParseException | BadJOSEException | JOSEException | IllegalArgumentException exception) {
            LOGGER.warn("Token rechazado: {}", exception.getClass().getSimpleName());
            throw new TokenInvalidoException("Token inválido.", exception);
        }
    }

    private static JWKSource<SecurityContext> clavesRemotas(String jwksUri) {
        try {
            return JWKSourceBuilder
                    .create(URI.create(jwksUri).toURL(), new DefaultResourceRetriever(
                            TIEMPO_ESPERA_JWKS_MS, TIEMPO_ESPERA_JWKS_MS, LIMITE_BYTES_JWKS))
                    .retrying(true)
                    .build();
        } catch (MalformedURLException | IllegalArgumentException exception) {
            throw new IllegalStateException("jwt.jwks-uri no es una URL válida: " + jwksUri, exception);
        }
    }
}
