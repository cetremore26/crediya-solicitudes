package co.com.crediya.model.solicitud;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import co.com.crediya.model.solicitud.exception.SolicitudValidationException;
import java.util.EnumSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class FiltroRevisionTest {

    @Test
    void defaultsToAllReviewStatesAndNormalizesLoanType() {
        FiltroRevision filtro = FiltroRevision.of(null, " vehiculo ", 0, 10);

        assertEquals(
                EnumSet.of(EstadoSolicitud.PENDIENTE_DE_REVISION, EstadoSolicitud.RECHAZADA,
                        EstadoSolicitud.REVISION_MANUAL),
                filtro.estados());
        assertEquals("VEHICULO", filtro.tipoPrestamo());
    }

    @Test
    void keepsRequestedStatesAndIgnoresBlankLoanType() {
        FiltroRevision filtro = FiltroRevision.of(
                Set.of(EstadoSolicitud.RECHAZADA), " ", 2, FiltroRevision.MAXIMUM_PAGE_SIZE);

        assertEquals(Set.of(EstadoSolicitud.RECHAZADA), filtro.estados());
        assertNull(filtro.tipoPrestamo());
        assertEquals(2, filtro.page());
    }

    @Test
    void rejectsInvalidPagingAndStates() {
        assertThrows(SolicitudValidationException.class, () -> FiltroRevision.of(null, null, -1, 10));
        assertThrows(SolicitudValidationException.class, () -> FiltroRevision.of(null, null, 0, 0));
        assertThrows(SolicitudValidationException.class, () -> FiltroRevision.of(null, null, 0, 51));
        assertThrows(SolicitudValidationException.class,
                () -> FiltroRevision.of(Set.of(EstadoSolicitud.APROBADA), null, 0, 10));
    }
}
