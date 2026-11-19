package co.com.crediya.usecase.solicitud;

import co.com.crediya.model.autenticacion.UsuarioAutenticado;
import co.com.crediya.model.solicitud.EstadoSolicitud;
import co.com.crediya.model.solicitud.Solicitud;
import co.com.crediya.model.solicitud.exception.AccesoDenegadoException;
import co.com.crediya.model.solicitud.exception.ClienteNoEncontradoException;
import co.com.crediya.model.solicitud.exception.SolicitudInvalidaException;
import co.com.crediya.model.solicitud.exception.TipoPrestamoNoExisteException;
import co.com.crediya.model.solicitud.gateways.ClienteGateway;
import co.com.crediya.model.solicitud.gateways.SolicitudRepository;
import co.com.crediya.model.solicitud.gateways.TipoPrestamoRepository;
import lombok.RequiredArgsConstructor;

/**
 * Registra una solicitud de préstamo. Solo orquesta el flujo: las reglas de qué es una
 * solicitud válida viven en {@link Solicitud} y las de quién puede registrarla en
 * {@link UsuarioAutenticado}.
 */
@RequiredArgsConstructor
public class RegistrarSolicitudUseCase {
    private final SolicitudRepository solicitudRepository;
    private final TipoPrestamoRepository tipoPrestamoRepository;
    private final ClienteGateway clienteGateway;

    public Solicitud execute(Solicitud solicitud, UsuarioAutenticado solicitante) {
        if (solicitud == null) {
            throw new SolicitudInvalidaException("La solicitud es obligatoria.");
        }

        Solicitud candidato = solicitud.normalizado();
        candidato.validar();
        validarSolicitantePropietario(solicitante, candidato.getDocumentoIdentidad());
        validarTipoPrestamoExiste(candidato.getTipoPrestamo());
        validarClienteExiste(candidato.getDocumentoIdentidad());

        return solicitudRepository.guardar(candidato.toBuilder()
                .estado(EstadoSolicitud.PENDIENTE_DE_REVISION)
                .build());
    }

    private void validarSolicitantePropietario(UsuarioAutenticado solicitante, String documento) {
        if (solicitante == null || !solicitante.esClienteConDocumento(documento)) {
            throw new AccesoDenegadoException("Solo puede registrar solicitudes de préstamo a su nombre.");
        }
    }

    private void validarTipoPrestamoExiste(String tipoPrestamo) {
        if (!tipoPrestamoRepository.existePorNombre(tipoPrestamo)) {
            throw new TipoPrestamoNoExisteException();
        }
    }

    private void validarClienteExiste(String documento) {
        if (!clienteGateway.existePorDocumentoIdentidad(documento)) {
            throw new ClienteNoEncontradoException();
        }
    }
}
