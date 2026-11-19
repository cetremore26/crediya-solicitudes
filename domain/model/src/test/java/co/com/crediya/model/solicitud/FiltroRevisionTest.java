package co.com.crediya.model.solicitud;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import co.com.crediya.model.solicitud.exception.SolicitudInvalidaException;
import java.util.EnumSet;
import java.util.HashSet;
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

    @Test
    void rejectsPagesWhoseOffsetDoesNotFitAnInt() {
        assertThrows(SolicitudInvalidaException.class,
                () -> FiltroRevision.de(null, null, 50_000_000, 50));
        assertEquals(42_949_672, FiltroRevision.de(null, null, Integer.MAX_VALUE / 50, 50).pagina());
    }

    @Test
    void validatesWhenBuiltDirectlyToo() {
        assertThrows(SolicitudInvalidaException.class,
                () -> new FiltroRevision(Set.of(), null, 0, 10));
        assertThrows(SolicitudInvalidaException.class,
                () -> new FiltroRevision(Set.of(EstadoSolicitud.APROBADA), null, 0, 10));
        assertThrows(SolicitudInvalidaException.class,
                () -> new FiltroRevision(Set.of(EstadoSolicitud.RECHAZADA), null, -1, 10));
        assertEquals("VIVIENDA",
                new FiltroRevision(Set.of(EstadoSolicitud.RECHAZADA), " vivienda ", 0, 10).tipoPrestamo());
    }

    @Test
    void statesCannotBeModifiedFromOutside() {
        Set<EstadoSolicitud> pedidos = new HashSet<>(Set.of(EstadoSolicitud.RECHAZADA));
        FiltroRevision filtro = FiltroRevision.de(pedidos, null, 0, 10);
        pedidos.add(EstadoSolicitud.REVISION_MANUAL);

        assertEquals(Set.of(EstadoSolicitud.RECHAZADA), filtro.estados());
        assertThrows(UnsupportedOperationException.class,
                () -> filtro.estados().add(EstadoSolicitud.APROBADA));
        assertThrows(UnsupportedOperationException.class,
                () -> FiltroRevision.de(null, null, 0, 10).estados().clear());
    }
}
