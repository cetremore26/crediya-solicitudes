package co.com.crediya.model.solicitud;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import co.com.crediya.model.solicitud.exception.SolicitudValidationException;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class SolicitudTest {

    @Test
    void normalizesDocumentAndLoanType() {
        Solicitud solicitud = validSolicitud().toBuilder()
                .documentoIdentidad(" 1020304050 ")
                .tipoPrestamo(" libre_inversion ")
                .build()
                .normalized();

        assertEquals("1020304050", solicitud.getDocumentoIdentidad());
        assertEquals("LIBRE_INVERSION", solicitud.getTipoPrestamo());
    }

    @Test
    void normalizesBlankLoanTypeToNull() {
        assertNull(validSolicitud().toBuilder().tipoPrestamo("  ").build()
                .normalized().getTipoPrestamo());
    }

    @Test
    void normalizedDoesNotModifyOriginal() {
        Solicitud original = validSolicitud().toBuilder().tipoPrestamo(" vehiculo ").build();

        original.normalized();

        assertEquals(" vehiculo ", original.getTipoPrestamo());
    }

    @Test
    void acceptsValidSolicitud() {
        assertDoesNotThrow(() -> validSolicitud().normalized().validate());
    }

    @Test
    void rejectsInvalidData() {
        List<Solicitud> invalid = List.of(
                validSolicitud().toBuilder().documentoIdentidad(null).build(),
                validSolicitud().toBuilder().documentoIdentidad("12ab").build(),
                validSolicitud().toBuilder().monto(null).build(),
                validSolicitud().toBuilder().monto(BigDecimal.ZERO).build(),
                validSolicitud().toBuilder().monto(new BigDecimal("-1")).build(),
                validSolicitud().toBuilder().plazoMeses(null).build(),
                validSolicitud().toBuilder().plazoMeses(0).build(),
                validSolicitud().toBuilder().tipoPrestamo(null).build(),
                validSolicitud().toBuilder().tipoPrestamo(" ").build());

        invalid.forEach(solicitud -> assertThrows(
                SolicitudValidationException.class, () -> solicitud.normalized().validate()));
    }

    @Test
    void calculatesMonthlyPaymentWithItsInterestRate() {
        Solicitud solicitud = validSolicitud().toBuilder()
                .monto(new BigDecimal("1200000"))
                .plazoMeses(12)
                .tasaInteres(BigDecimal.ZERO)
                .build();

        assertEquals(new BigDecimal("100000.00"), solicitud.cuotaMensual());
    }

    private Solicitud validSolicitud() {
        return Solicitud.builder()
                .documentoIdentidad("1020304050")
                .monto(new BigDecimal("5000000"))
                .plazoMeses(24)
                .tipoPrestamo("LIBRE_INVERSION")
                .build();
    }
}
