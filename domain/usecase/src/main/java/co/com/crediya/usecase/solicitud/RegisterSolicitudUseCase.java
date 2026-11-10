package co.com.crediya.usecase.solicitud;

import co.com.crediya.model.auth.AuthenticatedUser;
import co.com.crediya.model.auth.Rol;
import co.com.crediya.model.solicitud.EstadoSolicitud;
import co.com.crediya.model.solicitud.Solicitud;
import co.com.crediya.model.solicitud.exception.AccesoDenegadoException;
import co.com.crediya.model.solicitud.exception.ClienteNoEncontradoException;
import co.com.crediya.model.solicitud.exception.SolicitudValidationException;
import co.com.crediya.model.solicitud.exception.TipoPrestamoNoExisteException;
import co.com.crediya.model.solicitud.gateways.ClienteGateway;
import co.com.crediya.model.solicitud.gateways.SolicitudRepository;
import co.com.crediya.model.solicitud.gateways.TipoPrestamoRepository;
import lombok.RequiredArgsConstructor;

/**
 * Registra una solicitud de préstamo. Solo orquesta el flujo: las reglas de qué es una
 * solicitud válida viven en {@link Solicitud}.
 */
@RequiredArgsConstructor
public class RegisterSolicitudUseCase {
    private final SolicitudRepository solicitudRepository;
    private final TipoPrestamoRepository tipoPrestamoRepository;
    private final ClienteGateway clienteGateway;

    public Solicitud execute(Solicitud solicitud, AuthenticatedUser requester) {
        if (solicitud == null) {
            throw new SolicitudValidationException("La solicitud es obligatoria.");
        }

        Solicitud candidate = solicitud.normalized();
        candidate.validate();
        ensureRequesterOwnsDocument(requester, candidate.getDocumentoIdentidad());
        ensureTipoPrestamoExists(candidate.getTipoPrestamo());
        ensureClienteExists(candidate.getDocumentoIdentidad());

        return solicitudRepository.save(candidate.toBuilder()
                .estado(EstadoSolicitud.PENDIENTE_DE_REVISION)
                .build());
    }

    private void ensureRequesterOwnsDocument(AuthenticatedUser requester, String documento) {
        if (requester == null
                || requester.rol() != Rol.CLIENTE
                || !documento.equals(requester.documentoIdentidad())) {
            throw new AccesoDenegadoException();
        }
    }

    private void ensureTipoPrestamoExists(String tipoPrestamo) {
        if (!tipoPrestamoRepository.existsByNombre(tipoPrestamo)) {
            throw new TipoPrestamoNoExisteException();
        }
    }

    private void ensureClienteExists(String documento) {
        if (!clienteGateway.existsByDocumentoIdentidad(documento)) {
            throw new ClienteNoEncontradoException();
        }
    }
}
