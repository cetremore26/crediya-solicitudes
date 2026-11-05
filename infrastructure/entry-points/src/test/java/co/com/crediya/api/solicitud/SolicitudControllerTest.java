package co.com.crediya.api.solicitud;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.com.crediya.api.ApiExceptionHandler;
import co.com.crediya.model.solicitud.EstadoSolicitud;
import co.com.crediya.model.solicitud.Solicitud;
import co.com.crediya.model.solicitud.exception.ClienteNoEncontradoException;
import co.com.crediya.model.solicitud.exception.ClienteServiceUnavailableException;
import co.com.crediya.model.solicitud.exception.TipoPrestamoNoExisteException;
import co.com.crediya.usecase.solicitud.RegisterSolicitudUseCase;
import co.com.crediya.usecase.solicitud.exception.SolicitudValidationException;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

    private RegisterSolicitudUseCase useCase;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        useCase = mock(RegisterSolicitudUseCase.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new SolicitudController(useCase))
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }

    @Test
    void registersSolicitudAndReturnsCreatedWithPendingStatus() throws Exception {
        when(useCase.execute(any(Solicitud.class))).thenReturn(Solicitud.builder()
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
                .andExpect(jsonPath("$.tipo_prestamo").value("LIBRE_INVERSION"))
                .andExpect(jsonPath("$.documento_identidad").value("1020304050"));

        verify(useCase).execute(any(Solicitud.class));
    }

    @Test
    void returnsBadRequestForValidationErrors() throws Exception {
        when(useCase.execute(any(Solicitud.class)))
                .thenThrow(new SolicitudValidationException("El monto debe ser mayor a cero."));

        mockMvc.perform(postJson(VALID_BODY))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("DATOS_INVALIDOS"));
    }

    @Test
    void returnsBadRequestForUnknownLoanType() throws Exception {
        when(useCase.execute(any(Solicitud.class))).thenThrow(new TipoPrestamoNoExisteException());

        mockMvc.perform(postJson(VALID_BODY))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("TIPO_PRESTAMO_INVALIDO"));
    }

    @Test
    void returnsNotFoundForUnknownClient() throws Exception {
        when(useCase.execute(any(Solicitud.class))).thenThrow(new ClienteNoEncontradoException());

        mockMvc.perform(postJson(VALID_BODY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("CLIENTE_NO_ENCONTRADO"));
    }

    @Test
    void returnsServiceUnavailableWhenClientServiceFails() throws Exception {
        when(useCase.execute(any(Solicitud.class)))
                .thenThrow(new ClienteServiceUnavailableException(new RuntimeException("boom")));

        mockMvc.perform(postJson(VALID_BODY))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.codigo").value("SERVICIO_NO_DISPONIBLE"));
    }

    @Test
    void hidesUnexpectedErrorDetails() throws Exception {
        when(useCase.execute(any(Solicitud.class))).thenThrow(new IllegalStateException("secreto"));

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
        mockMvc.perform(get(URL))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.codigo").value("SOLICITUD_NO_SOPORTADA"));
    }

    private MockHttpServletRequestBuilder postJson(String body) {
        return post(URL).contentType("application/json").content(body);
    }
}
