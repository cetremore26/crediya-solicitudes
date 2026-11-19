package co.com.crediya.model.solicitud;

import co.com.crediya.model.solicitud.exception.SolicitudInvalidaException;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;

/**
 * Filtro y paginación para listar las solicitudes que necesitan una decisión manual.
 * Se construye con {@link #de} para que sea válido y esté normalizado.
 */
public record FiltroRevision(
        Set<EstadoSolicitud> estados, String tipoPrestamo, int pagina, int tamano) {
    public static final int TAMANO_MAXIMO_PAGINA = 50;
    private static final Set<EstadoSolicitud> ESTADOS_REVISABLES = EnumSet.copyOf(
            Arrays.stream(EstadoSolicitud.values()).filter(EstadoSolicitud::esRevisable).toList());

    /**
     * Construye un filtro: sin estados usa todos los estados revisables, y el tipo de préstamo
     * se normaliza como en {@link Solicitud#normalizarTipoPrestamo}.
     *
     * @throws SolicitudInvalidaException si la paginación o los estados no están permitidos
     */
    public static FiltroRevision de(
            Collection<EstadoSolicitud> estados, String tipoPrestamo, int pagina, int tamano) {
        if (pagina < 0) {
            throw new SolicitudInvalidaException("La página no puede ser negativa.");
        }
        if (tamano < 1 || tamano > TAMANO_MAXIMO_PAGINA) {
            throw new SolicitudInvalidaException(
                    "El tamaño de página debe estar entre 1 y " + TAMANO_MAXIMO_PAGINA + ".");
        }
        Set<EstadoSolicitud> estadosPedidos = estados == null || estados.isEmpty()
                ? ESTADOS_REVISABLES
                : EnumSet.copyOf(estados);
        if (!ESTADOS_REVISABLES.containsAll(estadosPedidos)) {
            throw new SolicitudInvalidaException(
                    "Solo se pueden listar solicitudes pendientes de revisión, rechazadas o en revisión manual.");
        }
        return new FiltroRevision(
                estadosPedidos, Solicitud.normalizarTipoPrestamo(tipoPrestamo), pagina, tamano);
    }
}
