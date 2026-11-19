package co.com.crediya.usecase.solicitud;

import co.com.crediya.model.autenticacion.UsuarioAutenticado;
import co.com.crediya.model.solicitud.DatosCliente;
import co.com.crediya.model.solicitud.EstadoSolicitud;
import co.com.crediya.model.solicitud.FiltroRevision;
import co.com.crediya.model.solicitud.Pagina;
import co.com.crediya.model.solicitud.Solicitud;
import co.com.crediya.model.solicitud.SolicitudRevision;
import co.com.crediya.model.solicitud.exception.AccesoDenegadoException;
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
 * mensual. Solo orquesta: las reglas del filtro viven en {@link FiltroRevision}, el cálculo
 * de la cuota en {@link Solicitud#cuotaMensual()} y quién puede revisar en
 * {@link UsuarioAutenticado#puedeRevisarSolicitudes()}.
 */
@RequiredArgsConstructor
public class ListarSolicitudesRevisionUseCase {
    private final SolicitudConsultaRepository solicitudConsultaRepository;
    private final DatosClienteGateway datosClienteGateway;

    public Pagina<SolicitudRevision> execute(
            Collection<EstadoSolicitud> estados, String tipoPrestamo, int page, int size,
            UsuarioAutenticado solicitante) {
        if (solicitante == null || !solicitante.puedeRevisarSolicitudes()) {
            throw new AccesoDenegadoException("Solo un asesor puede revisar solicitudes.");
        }
        FiltroRevision filtro = FiltroRevision.de(estados, tipoPrestamo, page, size);

        Pagina<Solicitud> solicitudes = solicitudConsultaRepository.buscarParaRevision(filtro);
        List<String> documentos = solicitudes.contenido().stream()
                .map(Solicitud::getDocumentoIdentidad)
                .distinct()
                .toList();
        if (documentos.isEmpty()) {
            return solicitudes.map(solicitud -> new SolicitudRevision(solicitud, null, BigDecimal.ZERO));
        }

        Map<String, DatosCliente> clientes = datosClienteGateway.buscarPorDocumentos(documentos).stream()
                .collect(Collectors.toMap(
                        DatosCliente::documentoIdentidad, Function.identity(), (first, duplicate) -> first));
        Map<String, BigDecimal> deudas = deudaMensualPorDocumento(documentos);

        return solicitudes.map(solicitud -> new SolicitudRevision(
                solicitud,
                clientes.get(solicitud.getDocumentoIdentidad()),
                deudas.getOrDefault(solicitud.getDocumentoIdentidad(), BigDecimal.ZERO)));
    }

    private Map<String, BigDecimal> deudaMensualPorDocumento(Collection<String> documentos) {
        return solicitudConsultaRepository.buscarAprobadasPorDocumentos(documentos).stream()
                .collect(Collectors.groupingBy(
                        Solicitud::getDocumentoIdentidad,
                        Collectors.reducing(BigDecimal.ZERO, Solicitud::cuotaMensual, BigDecimal::add)));
    }
}
