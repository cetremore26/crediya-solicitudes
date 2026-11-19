package co.com.crediya.api.solicitud;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.com.crediya.api.ManejadorErroresApi;
import co.com.crediya.model.autenticacion.UsuarioAutenticado;
import co.com.crediya.model.autenticacion.Rol;
import co.com.crediya.model.solicitud.DatosCliente;
import co.com.crediya.model.solicitud.EstadoSolicitud;
import co.com.crediya.model.solicitud.Pagina;
import co.com.crediya.model.solicitud.Solicitud;
import co.com.crediya.model.solicitud.SolicitudConTasa;
import co.com.crediya.model.solicitud.SolicitudRevision;
import co.com.crediya.model.solicitud.exception.AccesoDenegadoException;
import co.com.crediya.model.solicitud.exception.ClienteNoEncontradoException;
import co.com.crediya.model.solicitud.exception.ServicioUsuariosRechazoException;
import co.com.crediya.model.solicitud.exception.ServicioUsuariosNoDisponibleException;
import co.com.crediya.model.solicitud.exception.TipoPrestamoNoExisteException;
import co.com.crediya.usecase.solicitud.ListarSolicitudesRevisionUseCase;
import co.com.crediya.usecase.solicitud.RegistrarSolicitudUseCase;
import co.com.crediya.model.solicitud.exception.SolicitudInvalidaException;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class SolicitudControllerTest {
    private static final String URL = "/api/v1/solicitud";
    private static final String VALID_BODY = """
            {
              "documento_identidad": "1020304050",
              "monto": 5000000,
              "plazo_meses": 24,
              "tipo_prestamo": "LIBRE_INVERSION"
            }
            """;

    private static final UsuarioAutenticado CLIENTE = new UsuarioAutenticado(
            UUID.randomUUID(), "ana@example.com", Rol.CLIENTE, "1020304050");

    private RegistrarSolicitudUseCase useCase;
    private ListarSolicitudesRevisionUseCase listarUseCase;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        useCase = mock(RegistrarSolicitudUseCase.class);
        listarUseCase = mock(ListarSolicitudesRevisionUseCase.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new SolicitudController(useCase, listarUseCase))
                .setControllerAdvice(new ManejadorErroresApi())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(CLIENTE, null, List.of()));
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void registersSolicitudAndReturnsCreatedWithPendingStatus() throws Exception {
        when(useCase.execute(any(Solicitud.class), any(UsuarioAutenticado.class))).thenReturn(Solicitud.builder()
                .id(UUID.randomUUID())
                .documentoIdentidad("1020304050")
                .monto(new BigDecimal("5000000"))
                .plazoMeses(24)
                .tipoPrestamo("LIBRE_INVERSION")
                .estado(EstadoSolicitud.PENDIENTE_DE_REVISION)
                .build());

        mockMvc.perform(postJson(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("Pendiente de revisión"))
                .andExpect(jsonPath("$.estado_codigo").value("PENDIENTE_DE_REVISION"))
                .andExpect(jsonPath("$.tipo_prestamo").value("LIBRE_INVERSION"))
                .andExpect(jsonPath("$.documento_identidad").value("1020304050"));

        verify(useCase).execute(any(Solicitud.class), eq(CLIENTE));
    }

    @Test
    void returnsBadRequestForValidationErrors() throws Exception {
        when(useCase.execute(any(Solicitud.class), any(UsuarioAutenticado.class)))
                .thenThrow(new SolicitudInvalidaException("El monto debe ser mayor a cero."));

        mockMvc.perform(postJson(VALID_BODY))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("DATOS_INVALIDOS"));
    }

    @Test
    void returnsBadRequestForUnknownLoanType() throws Exception {
        when(useCase.execute(any(Solicitud.class), any(UsuarioAutenticado.class))).thenThrow(new TipoPrestamoNoExisteException());

        mockMvc.perform(postJson(VALID_BODY))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("TIPO_PRESTAMO_INVALIDO"));
    }

    @Test
    void returnsForbiddenWhenClientActsForAnotherPerson() throws Exception {
        when(useCase.execute(any(Solicitud.class), any(UsuarioAutenticado.class)))
                .thenThrow(new AccesoDenegadoException("Solo puede registrar solicitudes de préstamo a su nombre."));

        mockMvc.perform(postJson(VALID_BODY))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("ACCESO_DENEGADO"));
    }

    @Test
    void returnsUnprocessableContentForUnknownClient() throws Exception {
        when(useCase.execute(any(Solicitud.class), any(UsuarioAutenticado.class))).thenThrow(new ClienteNoEncontradoException());

        mockMvc.perform(postJson(VALID_BODY))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.codigo").value("CLIENTE_NO_ENCONTRADO"));
    }

    @Test
    void returnsServiceUnavailableWhenClientServiceFails() throws Exception {
        when(useCase.execute(any(Solicitud.class), any(UsuarioAutenticado.class)))
                .thenThrow(new ServicioUsuariosNoDisponibleException(new RuntimeException("boom")));

        mockMvc.perform(postJson(VALID_BODY))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.codigo").value("SERVICIO_NO_DISPONIBLE"));
    }

    @Test
    void hidesUnexpectedErrorDetails() throws Exception {
        when(useCase.execute(any(Solicitud.class), any(UsuarioAutenticado.class))).thenThrow(new IllegalStateException("secreto"));

        mockMvc.perform(postJson(VALID_BODY))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.codigo").value("ERROR_INTERNO"))
                .andExpect(jsonPath("$.mensaje").value(
                        "Ocurrió un error interno. Intente nuevamente más tarde."));
    }

    @Test
    void returnsBadRequestForMalformedBody() throws Exception {
        mockMvc.perform(postJson("{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("SOLICITUD_INVALIDA"));
    }

    @Test
    void returnsBadRequestWhenBodyIsNull() throws Exception {
        mockMvc.perform(postJson("null"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("SOLICITUD_INVALIDA"));
    }

    @Test
    void keepsHttpStatusForFrameworkErrors() throws Exception {
        mockMvc.perform(put(URL))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.codigo").value("SOLICITUD_NO_SOPORTADA"));
    }

    @Test
    void listsApplicationsForReviewWithClientDataAndPaging() throws Exception {
        Solicitud solicitud = Solicitud.builder()
                .id(UUID.randomUUID())
                .documentoIdentidad("1020304050")
                .monto(new BigDecimal("5000000"))
                .plazoMeses(24)
                .tipoPrestamo("VEHICULO")
                .estado(EstadoSolicitud.REVISION_MANUAL)
                .build();
        DatosCliente cliente = new DatosCliente(
                "1020304050", "Ana", "Pérez", "ana@example.com", new BigDecimal("4000000"));
        when(listarUseCase.execute(
                        List.of(EstadoSolicitud.REVISION_MANUAL), "VEHICULO", 1, 5, CLIENTE))
                .thenReturn(new Pagina<>(
                        List.of(new SolicitudRevision(new SolicitudConTasa(solicitud, new BigDecimal("14.00")), cliente, new BigDecimal("250000.00"))),
                        1, 5, 6));

        mockMvc.perform(get(URL)
                        .param("estado", "REVISION_MANUAL")
                        .param("tipo_prestamo", "VEHICULO")
                        .param("pagina", "1")
                        .param("tamano", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total_elementos").value(6))
                .andExpect(jsonPath("$.total_paginas").value(2))
                .andExpect(jsonPath("$.contenido[0].correo_electronico").value("ana@example.com"))
                .andExpect(jsonPath("$.contenido[0].nombre_completo").value("Ana Pérez"))
                .andExpect(jsonPath("$.contenido[0].estado_solicitud").value("Revisión manual"))
                .andExpect(jsonPath("$.contenido[0].estado_codigo").value("REVISION_MANUAL"))
                .andExpect(jsonPath("$.contenido[0].tasa_interes").value(14.00))
                .andExpect(jsonPath("$.contenido[0].salario_base").value(4000000))
                .andExpect(jsonPath("$.contenido[0].deuda_total_mensual_solicitudes_aprobadas")
                        .value(250000.00));
    }

    @Test
    void usesDefaultPagingAndToleratesMissingClientData() throws Exception {
        Solicitud solicitud = Solicitud.builder()
                .id(UUID.randomUUID())
                .documentoIdentidad("999999")
                .monto(new BigDecimal("1000000"))
                .plazoMeses(12)
                .tipoPrestamo("VEHICULO")
                .estado(EstadoSolicitud.RECHAZADA)
                .build();
        when(listarUseCase.execute(null, null, 0, 10, CLIENTE)).thenReturn(new Pagina<>(
                List.of(new SolicitudRevision(new SolicitudConTasa(solicitud, new BigDecimal("14.00")), null, BigDecimal.ZERO)), 0, 10, 1));

        mockMvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido[0].correo_electronico").isEmpty())
                .andExpect(jsonPath("$.contenido[0].estado_solicitud").value("Rechazada"));
    }

    @Test
    void returnsBadRequestForUnknownStateFilter() throws Exception {
        mockMvc.perform(get(URL).param("estado", "INEXISTENTE"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("PARAMETRO_INVALIDO"));
    }

    @Test
    void returnsBadRequestForInvalidPaging() throws Exception {
        when(listarUseCase.execute(null, null, 0, 500, CLIENTE))
                .thenThrow(new SolicitudInvalidaException("El tamaño de página es inválido."));

        mockMvc.perform(get(URL).param("tamano", "500"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("DATOS_INVALIDOS"));
    }

    @Test
    void returnsBadGatewayWhenUsersServiceRejectsTheQuery() throws Exception {
        when(useCase.execute(any(), any())).thenThrow(new ServicioUsuariosRechazoException(403));

        mockMvc.perform(postJson(VALID_BODY))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.codigo").value("INTEGRACION_USUARIOS_FALLIDA"));
    }

    private MockHttpServletRequestBuilder postJson(String body) {
        return post(URL)
                .contentType("application/json")
                .content(body);
    }
}
