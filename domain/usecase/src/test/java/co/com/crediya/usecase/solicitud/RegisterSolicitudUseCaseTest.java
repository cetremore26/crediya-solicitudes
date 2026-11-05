package co.com.crediya.usecase.solicitud;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import co.com.crediya.model.solicitud.EstadoSolicitud;
import co.com.crediya.model.solicitud.Solicitud;
import co.com.crediya.model.solicitud.exception.ClienteNoEncontradoException;
import co.com.crediya.model.solicitud.exception.TipoPrestamoNoExisteException;
import co.com.crediya.model.solicitud.gateways.ClienteGateway;
import co.com.crediya.model.solicitud.gateways.SolicitudRepository;
import co.com.crediya.model.solicitud.gateways.TipoPrestamoRepository;
import co.com.crediya.usecase.solicitud.exception.SolicitudValidationException;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RegisterSolicitudUseCaseTest {
    private static final String TIPO_EXISTENTE = "LIBRE_INVERSION";
    private static final String DOCUMENTO_EXISTENTE = "1020304050";

    private final SolicitudRepository solicitudRepository = solicitud -> solicitud.toBuilder()
            .id(UUID.randomUUID())
            .build();
    private final TipoPrestamoRepository tipoPrestamoRepository = TIPO_EXISTENTE::equals;
    private final ClienteGateway clienteGateway = DOCUMENTO_EXISTENTE::equals;
    private final RegisterSolicitudUseCase useCase = new RegisterSolicitudUseCase(
            solicitudRepository, tipoPrestamoRepository, clienteGateway);

    @Test
    void registersSolicitudWithPendingReviewStatus() {
        Solicitud saved = useCase.execute(validSolicitud());

        assertNotNull(saved.getId());
        assertEquals(EstadoSolicitud.PENDIENTE_DE_REVISION, saved.getEstado());
    }

    @Test
    void normalizesDocumentAndLoanType() {
        Solicitud saved = useCase.execute(validSolicitud().toBuilder()
                .documentoIdentidad(" " + DOCUMENTO_EXISTENTE + " ")
                .tipoPrestamo(" libre_inversion ")
                .build());

        assertEquals(DOCUMENTO_EXISTENTE, saved.getDocumentoIdentidad());
        assertEquals(TIPO_EXISTENTE, saved.getTipoPrestamo());
    }

    @Test
    void rejectsNullSolicitud() {
        assertThrows(SolicitudValidationException.class, () -> useCase.execute(null));
    }

    @Test
    void rejectsInvalidFields() {
        List<Solicitud> invalidSolicitudes = List.of(
                validSolicitud().toBuilder().documentoIdentidad(null).build(),
                validSolicitud().toBuilder().documentoIdentidad("12ab").build(),
                validSolicitud().toBuilder().monto(null).build(),
                validSolicitud().toBuilder().monto(BigDecimal.ZERO).build(),
                validSolicitud().toBuilder().monto(new BigDecimal("-1")).build(),
                validSolicitud().toBuilder().plazoMeses(null).build(),
                validSolicitud().toBuilder().plazoMeses(0).build(),
                validSolicitud().toBuilder().tipoPrestamo(null).build(),
                validSolicitud().toBuilder().tipoPrestamo(" ").build());

        invalidSolicitudes.forEach(solicitud -> assertThrows(
                SolicitudValidationException.class, () -> useCase.execute(solicitud)));
    }

    @Test
    void rejectsUnknownLoanType() {
        Solicitud solicitud = validSolicitud().toBuilder().tipoPrestamo("INEXISTENTE").build();

        assertThrows(TipoPrestamoNoExisteException.class, () -> useCase.execute(solicitud));
    }

    @Test
    void rejectsUnknownClient() {
        Solicitud solicitud = validSolicitud().toBuilder().documentoIdentidad("99999999").build();

        assertThrows(ClienteNoEncontradoException.class, () -> useCase.execute(solicitud));
    }

    private Solicitud validSolicitud() {
        return Solicitud.builder()
                .documentoIdentidad(DOCUMENTO_EXISTENTE)
                .monto(new BigDecimal("5000000"))
                .plazoMeses(24)
                .tipoPrestamo(TIPO_EXISTENTE)
                .build();
    }
}
