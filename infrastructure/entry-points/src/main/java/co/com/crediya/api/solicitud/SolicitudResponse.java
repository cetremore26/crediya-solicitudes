package co.com.crediya.api.solicitud;

import co.com.crediya.model.solicitud.Solicitud;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.UUID;

public record SolicitudResponse(
        UUID id,
        @JsonProperty("documento_identidad") String documentoIdentidad,
        BigDecimal monto,
        @JsonProperty("plazo_meses") Integer plazoMeses,
        @JsonProperty("tipo_prestamo") String tipoPrestamo,
        String estado) {

    public static SolicitudResponse from(Solicitud solicitud) {
        return new SolicitudResponse(
                solicitud.getId(),
                solicitud.getDocumentoIdentidad(),
                solicitud.getMonto(),
                solicitud.getPlazoMeses(),
                solicitud.getTipoPrestamo(),
                solicitud.getEstado().getDescripcion());
    }
}
