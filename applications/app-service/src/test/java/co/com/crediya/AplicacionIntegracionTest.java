package co.com.crediya;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Arranca la aplicación completa con su configuración real (solo cambian la base, por H2, y la
 * dirección del servicio de autenticación): cadena de seguridad, verificación RS256 contra un
 * JWKS remoto, cliente HTTP del servicio de usuarios, casos de uso, Flyway y
 * {@code ddl-auto: validate}. El servicio de autenticación se reemplaza por un servidor HTTP
 * local que publica el JWKS y responde las consultas de usuarios.
 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:crediya-solicitudes-it;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;"
                + "DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "jwt.allow-insecure-jwks-uri=true"
})
@AutoConfigureMockMvc
class AplicacionIntegracionTest {
    private static final String DOCUMENTO_CLIENTE = "1020304050";
    private static final String DOCUMENTO_SIN_USUARIO = "5550001112";
    private static final RSAKey CLAVE_AUTENTICACION = generarClave();
    private static final HttpServer AUTENTICACION = iniciarServicioAutenticacion();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @DynamicPropertySource
    static void servicioAutenticacion(DynamicPropertyRegistry registry) {
        String url = "http://127.0.0.1:" + AUTENTICACION.getAddress().getPort();
        registry.add("usuarios.service.url", () -> url);
        registry.add("jwt.jwks-uri", () -> url + "/.well-known/jwks.json");
    }

    @AfterAll
    static void detenerServicioAutenticacion() {
        AUTENTICACION.stop(0);
    }

    @Test
    void appliesEveryMigrationAndSeedsTheLoanTypes() {
        List<String> migraciones = jdbcTemplate.queryForList(
                "SELECT \"version\" FROM \"flyway_schema_history\" "
                        + "WHERE \"type\" = 'SQL' AND \"success\" = TRUE ORDER BY \"installed_rank\"",
                String.class);
        Integer tipos = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM tipos_prestamo", Integer.class);

        assertEquals(List.of("1", "2"), migraciones);
        assertEquals(4, tipos);
    }

    @Test
    void clientRegistersAnApplicationAndAnAdvisorSeesItForReview() throws Exception {
        String cliente = token("CLIENTE", DOCUMENTO_CLIENTE);

        mockMvc.perform(post("/api/v1/solicitud")
                        .header("Authorization", "Bearer " + cliente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(solicitud(DOCUMENTO_CLIENTE)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado_codigo").value("PENDIENTE_DE_REVISION"));

        mockMvc.perform(get("/api/v1/solicitud")
                        .param("tipo_prestamo", "vehiculo")
                        .header("Authorization", "Bearer " + token("ASESOR", null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total_elementos").value(1))
                .andExpect(jsonPath("$.contenido[0].correo_electronico").value("ana@example.com"))
                .andExpect(jsonPath("$.contenido[0].nombre_completo").value("Ana Pérez"))
                .andExpect(jsonPath("$.contenido[0].tasa_interes").value(14.0))
                .andExpect(jsonPath("$.contenido[0].deuda_total_mensual_solicitudes_aprobadas").value(0));
    }

    @Test
    void rejectsAnApplicationForAClientThatDoesNotExist() throws Exception {
        mockMvc.perform(post("/api/v1/solicitud")
                        .header("Authorization", "Bearer " + token("CLIENTE", DOCUMENTO_SIN_USUARIO))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(solicitud(DOCUMENTO_SIN_USUARIO)))
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    void enforcesAuthenticationAndRoles() throws Exception {
        mockMvc.perform(get("/api/v1/solicitud"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("NO_AUTENTICADO"));
        mockMvc.perform(get("/api/v1/solicitud")
                        .header("Authorization", "Bearer " + token("CLIENTE", DOCUMENTO_CLIENTE)))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/v1/solicitud")
                        .header("Authorization", "Bearer " + token("ASESOR", null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(solicitud(DOCUMENTO_CLIENTE)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/solicitud")
                        .header("Authorization", "Bearer " + firmar(otraClave(), "ASESOR", null)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void publishesHealthButNotTheApiDocs() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
        mockMvc.perform(get("/v3/api-docs")).andExpect(status().isNotFound());
    }

    private static String solicitud(String documento) {
        return """
                {
                  "documento_identidad": "%s",
                  "monto": 5000000,
                  "plazo_meses": 24,
                  "tipo_prestamo": "vehiculo"
                }
                """.formatted(documento);
    }

    private static String token(String rol, String documento) throws Exception {
        return firmar(CLAVE_AUTENTICACION, rol, documento);
    }

    private static String firmar(RSAKey clave, String rol, String documento) throws Exception {
        Instant ahora = Instant.now();
        SignedJWT jwt = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(clave.getKeyID()).build(),
                new JWTClaimsSet.Builder()
                        .issuer("crediya-authentication")
                        .subject(UUID.randomUUID().toString())
                        .claim("correo_electronico", "persona@crediya.com")
                        .claim("rol", rol)
                        .claim("documento_identidad", documento)
                        .issueTime(Date.from(ahora))
                        .notBeforeTime(Date.from(ahora))
                        .expirationTime(Date.from(ahora.plusSeconds(600)))
                        .build());
        jwt.sign(new RSASSASigner(clave));
        return jwt.serialize();
    }

    /** Hace de servicio de autenticación: JWKS, consulta por documento y consulta por lote. */
    private static HttpServer iniciarServicioAutenticacion() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            server.createContext("/.well-known/jwks.json", exchange -> responder(exchange, 200,
                    new JWKSet(CLAVE_AUTENTICACION.toPublicJWK()).toString()));
            server.createContext("/api/v1/usuarios/documento/", exchange -> {
                boolean existe = exchange.getRequestURI().getPath().endsWith("/" + DOCUMENTO_CLIENTE);
                responder(exchange, existe ? 200 : 404, existe ? usuarioJson() : "{}");
            });
            server.createContext("/api/v1/usuarios", exchange -> {
                String consulta = exchange.getRequestURI().getQuery();
                boolean incluye = consulta != null && consulta.contains(DOCUMENTO_CLIENTE);
                responder(exchange, 200, incluye ? "[" + usuarioJson() + "]" : "[]");
            });
            server.start();
            return server;
        } catch (IOException exception) {
            throw new IllegalStateException(exception);
        }
    }

    private static String usuarioJson() {
        return """
                {"documento_identidad":"%s","nombres":"Ana","apellidos":"Pérez",
                 "correo_electronico":"ana@example.com","salario_base":4000000}
                """.formatted(DOCUMENTO_CLIENTE);
    }

    private static void responder(HttpExchange exchange, int estado, String cuerpo) throws IOException {
        byte[] bytes = cuerpo.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(estado, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }

    private static RSAKey generarClave() {
        try {
            return new RSAKeyGenerator(2048).keyIDFromThumbprint(true).generate();
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }

    private static RSAKey otraClave() {
        return generarClave();
    }
}
