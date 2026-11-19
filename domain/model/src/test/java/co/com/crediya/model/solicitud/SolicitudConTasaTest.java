package co.com.crediya.model.solicitud;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class SolicitudConTasaTest {
    private static final Solicitud SOLICITUD = Solicitud.builder()
            .documentoIdentidad("1020304050")
            .monto(new BigDecimal("1200000"))
            .plazoMeses(12)
            .tipoPrestamo("VEHICULO")
            .build();

    @Test
    void calculatesMonthlyPaymentWithItsInterestRate() {
        assertEquals(new BigDecimal("100000.00"),
                new SolicitudConTasa(SOLICITUD, BigDecimal.ZERO).cuotaMensual());
        assertEquals(CalculadoraCuotaMensual.calcular(new BigDecimal("1200000"), 12, new BigDecimal("18")),
                new SolicitudConTasa(SOLICITUD, new BigDecimal("18")).cuotaMensual());
    }

    @Test
    void exposesTheApplicantDocument() {
        assertEquals("1020304050", new SolicitudConTasa(SOLICITUD, BigDecimal.TEN).documentoIdentidad());
    }

    @Test
    void requiresTheApplicationAndANonNegativeRate() {
        assertThrows(IllegalArgumentException.class, () -> new SolicitudConTasa(null, BigDecimal.ONE));
        assertThrows(IllegalArgumentException.class, () -> new SolicitudConTasa(SOLICITUD, null));
        assertThrows(IllegalArgumentException.class,
                () -> new SolicitudConTasa(SOLICITUD, new BigDecimal("-1")));
    }
}
