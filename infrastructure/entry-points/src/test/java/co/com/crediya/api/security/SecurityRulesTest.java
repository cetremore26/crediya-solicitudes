package co.com.crediya.api.security;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.com.crediya.api.solicitud.SolicitudController;
import co.com.crediya.model.auth.AuthenticatedUser;
import co.com.crediya.model.auth.Rol;
import co.com.crediya.model.auth.exception.InvalidTokenException;
import co.com.crediya.model.auth.gateways.TokenVerifier;
import co.com.crediya.model.solicitud.EstadoSolicitud;
import co.com.crediya.model.solicitud.PageResult;
import co.com.crediya.model.solicitud.Solicitud;
import co.com.crediya.usecase.solicitud.ListSolicitudesRevisionUseCase;
import co.com.crediya.usecase.solicitud.RegisterSolicitudUseCase;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/**
 * Reglas de acceso por ruta y rol con la cadena de seguridad real ({@link SecurityConfig} y
 * {@link JwtAuthenticationFilter}). El verificador de tokens se simula: cada token de prueba
 * es el nombre del rol.
 */
@WebMvcTest(controllers = SolicitudController.class)
@Import(SecurityConfig.class)
class SecurityRulesTest {
    private static final String BODY = """
            {"documento_identidad":"1020304050","monto":5000000,"plazo_meses":24,
             "tipo_prestamo":"VEHICULO"}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TokenVerifier tokenVerifier;
    @MockitoBean
    private RegisterSolicitudUseCase registerSolicitudUseCase;
    @MockitoBean
    private ListSolicitudesRevisionUseCase listSolicitudesRevisionUseCase;

    @BeforeEach
    void setUp() {
        for (Rol rol : Rol.values()) {
            when(tokenVerifier.verify(rol.name())).thenReturn(
                    new AuthenticatedUser(UUID.randomUUID(), "u@crediya.com", rol, "1020304050"));
        }
        when(tokenVerifier.verify("invalido")).thenThrow(new InvalidTokenException("Token inválido."));
        when(registerSolicitudUseCase.execute(any(), any())).thenReturn(Solicitud.builder()
                .id(UUID.randomUUID())
                .estado(EstadoSolicitud.PENDIENTE_DE_REVISION)
                .build());
        when(listSolicitudesRevisionUseCase.execute(any(), any(), anyInt(), anyInt(), any()))
                .thenReturn(new PageResult<>(List.of(), 0, 10, 0));
    }

    @Test
    void everyRouteRequiresAValidToken() throws Exception {
        mockMvc.perform(get("/api/v1/solicitud"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("NO_AUTENTICADO"));
        mockMvc.perform(post("/api/v1/solicitud").contentType("application/json").content(BODY))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(as("invalido", get("/api/v1/solicitud")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void onlyClientsRegisterApplications() throws Exception {
        mockMvc.perform(as("CLIENTE", register())).andExpect(status().isCreated());
        for (String staff : List.of("ASESOR", "ADMIN")) {
            mockMvc.perform(as(staff, register()))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.codigo").value("ACCESO_DENEGADO"));
        }
    }

    @Test
    void onlyAdvisorsListApplicationsForReview() throws Exception {
        mockMvc.perform(as("ASESOR", get("/api/v1/solicitud"))).andExpect(status().isOk());
        for (String other : List.of("CLIENTE", "ADMIN")) {
            mockMvc.perform(as(other, get("/api/v1/solicitud"))).andExpect(status().isForbidden());
        }
    }

    private static MockHttpServletRequestBuilder register() {
        return post("/api/v1/solicitud").contentType("application/json").content(BODY);
    }

    private static MockHttpServletRequestBuilder as(String token, MockHttpServletRequestBuilder request) {
        return request.header("Authorization", "Bearer " + token);
    }
}
