package co.com.crediya.usecase.solicitud;

import co.com.crediya.model.solicitud.EstadoSolicitud;
import co.com.crediya.model.solicitud.Solicitud;
import co.com.crediya.model.solicitud.exception.ClienteNoEncontradoException;
import co.com.crediya.model.solicitud.exception.TipoPrestamoNoExisteException;
import co.com.crediya.model.solicitud.gateways.ClienteGateway;
import co.com.crediya.model.solicitud.gateways.SolicitudRepository;
import co.com.crediya.model.solicitud.gateways.TipoPrestamoRepository;
import co.com.crediya.usecase.solicitud.exception.SolicitudValidationException;
import java.math.BigDecimal;
import java.util.Locale;
import java.util.regex.Pattern;

public class RegisterSolicitudUseCase {
    private static final Pattern DOCUMENTO_PATTERN = Pattern.compile("^\\d{5,15}$");

    private final SolicitudRepository solicitudRepository;
    private final TipoPrestamoRepository tipoPrestamoRepository;
    private final ClienteGateway clienteGateway;

    public RegisterSolicitudUseCase(
            SolicitudRepository solicitudRepository,
            TipoPrestamoRepository tipoPrestamoRepository,
            ClienteGateway clienteGateway) {
        this.solicitudRepository = solicitudRepository;
        this.tipoPrestamoRepository = tipoPrestamoRepository;
        this.clienteGateway = clienteGateway;
    }

    public Solicitud execute(Solicitud solicitud) {
        validate(solicitud);

        String documento = solicitud.getDocumentoIdentidad().trim();
        String tipoPrestamo = solicitud.getTipoPrestamo().trim().toUpperCase(Locale.ROOT);

        if (!tipoPrestamoRepository.existsByNombre(tipoPrestamo)) {
            throw new TipoPrestamoNoExisteException();
        }
        if (!clienteGateway.existsByDocumentoIdentidad(documento)) {
            throw new ClienteNoEncontradoException();
        }

        Solicitud solicitudToSave = solicitud.toBuilder()
                .documentoIdentidad(documento)
                .tipoPrestamo(tipoPrestamo)
                .estado(EstadoSolicitud.PENDIENTE_DE_REVISION)
                .build();
        return solicitudRepository.save(solicitudToSave);
    }

    private void validate(Solicitud solicitud) {
        if (solicitud == null) {
            throw new SolicitudValidationException("La solicitud es obligatoria.");
        }
        String documento = solicitud.getDocumentoIdentidad();
        if (documento == null || !DOCUMENTO_PATTERN.matcher(documento.trim()).matches()) {
            throw new SolicitudValidationException(
                    "El documento de identidad debe contener entre 5 y 15 dígitos.");
        }
        BigDecimal monto = solicitud.getMonto();
        if (monto == null || monto.signum() <= 0) {
            throw new SolicitudValidationException("El monto debe ser mayor a cero.");
        }
        Integer plazo = solicitud.getPlazoMeses();
        if (plazo == null || plazo <= 0) {
            throw new SolicitudValidationException("El plazo debe ser mayor a cero meses.");
        }
        String tipo = solicitud.getTipoPrestamo();
        if (tipo == null || tipo.isBlank()) {
            throw new SolicitudValidationException("El tipo de préstamo es obligatorio.");
        }
    }
}
