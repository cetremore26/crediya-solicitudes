package co.com.crediya.usecase.solicitud;

import co.com.crediya.model.solicitud.DatosCliente;
import co.com.crediya.model.solicitud.EstadoSolicitud;
import co.com.crediya.model.solicitud.FiltroRevision;
import co.com.crediya.model.solicitud.PageResult;
import co.com.crediya.model.solicitud.Solicitud;
import co.com.crediya.model.solicitud.SolicitudRevision;
import co.com.crediya.model.solicitud.gateways.DatosClienteGateway;
import co.com.crediya.model.solicitud.gateways.SolicitudConsultaRepository;
import co.com.crediya.model.solicitud.exception.SolicitudValidationException;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ListSolicitudesRevisionUseCase {
    public static final int MAXIMUM_PAGE_SIZE = 50;
    private static final Set<EstadoSolicitud> REVIEW_STATES = EnumSet.of(
            EstadoSolicitud.PENDIENTE_DE_REVISION,
            EstadoSolicitud.RECHAZADA,
            EstadoSolicitud.REVISION_MANUAL);

    private final SolicitudConsultaRepository solicitudConsultaRepository;
    private final DatosClienteGateway datosClienteGateway;

    public ListSolicitudesRevisionUseCase(
            SolicitudConsultaRepository solicitudConsultaRepository,
            DatosClienteGateway datosClienteGateway) {
        this.solicitudConsultaRepository = solicitudConsultaRepository;
        this.datosClienteGateway = datosClienteGateway;
    }

    public PageResult<SolicitudRevision> execute(
            Collection<EstadoSolicitud> estados, String tipoPrestamo, int page, int size) {
        FiltroRevision filtro = buildFilter(estados, tipoPrestamo, page, size);

        PageResult<Solicitud> solicitudes = solicitudConsultaRepository.findForReview(filtro);
        List<String> documentos = solicitudes.content().stream()
                .map(Solicitud::getDocumentoIdentidad)
                .distinct()
                .toList();
        if (documentos.isEmpty()) {
            return solicitudes.map(solicitud -> new SolicitudRevision(solicitud, null, BigDecimal.ZERO));
        }

        Map<String, DatosCliente> clientes = datosClienteGateway.findByDocumentos(documentos).stream()
                .collect(Collectors.toMap(DatosCliente::documentoIdentidad, Function.identity()));
        Map<String, BigDecimal> deudas = monthlyDebtByDocument(documentos);

        return solicitudes.map(solicitud -> new SolicitudRevision(
                solicitud,
                clientes.get(solicitud.getDocumentoIdentidad()),
                deudas.getOrDefault(solicitud.getDocumentoIdentidad(), BigDecimal.ZERO)));
    }

    private Map<String, BigDecimal> monthlyDebtByDocument(Collection<String> documentos) {
        return solicitudConsultaRepository.findApprovedByDocumentos(documentos).stream()
                .collect(Collectors.groupingBy(
                        Solicitud::getDocumentoIdentidad,
                        Collectors.reducing(BigDecimal.ZERO, this::monthlyPayment, BigDecimal::add)));
    }

    private BigDecimal monthlyPayment(Solicitud solicitud) {
        return CuotaMensualCalculator.calculate(
                solicitud.getMonto(), solicitud.getPlazoMeses(), solicitud.getTasaInteres());
    }

    private FiltroRevision buildFilter(
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
        String tipo = tipoPrestamo == null || tipoPrestamo.isBlank()
                ? null
                : tipoPrestamo.trim().toUpperCase(Locale.ROOT);
        return new FiltroRevision(requestedStates, tipo, page, size);
    }
}
