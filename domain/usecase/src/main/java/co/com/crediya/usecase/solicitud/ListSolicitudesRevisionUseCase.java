package co.com.crediya.usecase.solicitud;

import co.com.crediya.model.solicitud.DatosCliente;
import co.com.crediya.model.solicitud.EstadoSolicitud;
import co.com.crediya.model.solicitud.FiltroRevision;
import co.com.crediya.model.solicitud.PageResult;
import co.com.crediya.model.solicitud.Solicitud;
import co.com.crediya.model.solicitud.SolicitudRevision;
import co.com.crediya.model.solicitud.gateways.DatosClienteGateway;
import co.com.crediya.model.solicitud.gateways.SolicitudConsultaRepository;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

/**
 * Lista las solicitudes que requieren revisión manual, con los datos del cliente y su deuda
 * mensual. Solo orquesta: las reglas del filtro viven en {@link FiltroRevision} y el cálculo
 * de la cuota en {@link Solicitud#cuotaMensual()}.
 */
@RequiredArgsConstructor
public class ListSolicitudesRevisionUseCase {
    private final SolicitudConsultaRepository solicitudConsultaRepository;
    private final DatosClienteGateway datosClienteGateway;

    public PageResult<SolicitudRevision> execute(
            Collection<EstadoSolicitud> estados, String tipoPrestamo, int page, int size) {
        FiltroRevision filtro = FiltroRevision.of(estados, tipoPrestamo, page, size);

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
                        Collectors.reducing(BigDecimal.ZERO, Solicitud::cuotaMensual, BigDecimal::add)));
    }
}
