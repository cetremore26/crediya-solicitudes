package co.com.crediya.model.solicitud;

import co.com.crediya.model.solicitud.exception.SolicitudValidationException;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;

/**
 * Filter and paging options to list applications that need a manual decision.
 * Use {@link #of} to build a valid, normalized filter.
 */
public record FiltroRevision(
        Set<EstadoSolicitud> estados, String tipoPrestamo, int page, int size) {
    public static final int MAXIMUM_PAGE_SIZE = 50;
    private static final Set<EstadoSolicitud> REVIEW_STATES = EnumSet.of(
            EstadoSolicitud.PENDIENTE_DE_REVISION,
            EstadoSolicitud.RECHAZADA,
            EstadoSolicitud.REVISION_MANUAL);

    /**
     * Builds a filter: without states it uses every review state, and the loan type is
     * normalized like {@link Solicitud#normalizeTipoPrestamo}.
     *
     * @throws SolicitudValidationException when the paging or the states are not allowed
     */
    public static FiltroRevision of(
            Collection<EstadoSolicitud> estados, String tipoPrestamo, int page, int size) {
        if (page < 0) {
            throw new SolicitudValidationException("La página no puede ser negativa.");
        }
        if (size < 1 || size > MAXIMUM_PAGE_SIZE) {
            throw new SolicitudValidationException(
                    "El tamaño de página debe estar entre 1 y " + MAXIMUM_PAGE_SIZE + ".");
        }
        Set<EstadoSolicitud> requestedStates = estados == null || estados.isEmpty()
                ? REVIEW_STATES
                : EnumSet.copyOf(estados);
        if (!REVIEW_STATES.containsAll(requestedStates)) {
            throw new SolicitudValidationException(
                    "Solo se pueden listar solicitudes pendientes de revisión, rechazadas o en revisión manual.");
        }
        return new FiltroRevision(
                requestedStates, Solicitud.normalizeTipoPrestamo(tipoPrestamo), page, size);
    }
}
