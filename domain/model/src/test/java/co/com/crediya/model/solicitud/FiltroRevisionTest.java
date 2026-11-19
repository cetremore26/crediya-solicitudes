package co.com.crediya.model.solicitud;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import co.com.crediya.model.solicitud.exception.SolicitudInvalidaException;
import java.util.EnumSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class FiltroRevisionTest {

    @Test
    void defaultsToAllReviewStatesAndNormalizesLoanType() {
        FiltroRevision filtro = FiltroRevision.de(null, " vehiculo ", 0, 10);

        assertEquals(
                EnumSet.of(EstadoSolicitud.PENDIENTE_DE_REVISION, EstadoSolicitud.RECHAZADA,
                        EstadoSolicitud.REVISION_MANUAL),
                filtro.estados());
        assertEquals("VEHICULO", filtro.tipoPrestamo());
    }

    @Test
    void keepsRequestedStatesAndIgnoresBlankLoanType() {
        FiltroRevision filtro = FiltroRevision.de(
                Set.of(EstadoSolicitud.RECHAZADA), " ", 2, FiltroRevision.TAMANO_MAXIMO_PAGINA);

        assertEquals(Set.of(EstadoSolicitud.RECHAZADA), filtro.estados());
        assertNull(filtro.tipoPrestamo());
        assertEquals(2, filtro.pagina());
    }

    @Test
    void rejectsInvalidPagingAndStates() {
        assertThrows(SolicitudInvalidaException.class, () -> FiltroRevision.de(null, null, -1, 10));
        assertThrows(SolicitudInvalidaException.class, () -> FiltroRevision.de(null, null, 0, 0));
        assertThrows(SolicitudInvalidaException.class, () -> FiltroRevision.de(null, null, 0, 51));
        assertThrows(SolicitudInvalidaException.class,
                () -> FiltroRevision.de(Set.of(EstadoSolicitud.APROBADA), null, 0, 10));
    }
}
