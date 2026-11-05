package co.com.crediya.api.solicitud;

import co.com.crediya.model.solicitud.Solicitud;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

public record SolicitudRequest(
        @JsonProperty("documento_identidad") String documentoIdentidad,
        BigDecimal monto,
        @JsonProperty("plazo_meses") Integer plazoMeses,
        @JsonProperty("tipo_prestamo") String tipoPrestamo) {

    public Solicitud toDomain() {
        return Solicitud.builder()
                .documentoIdentidad(documentoIdentidad)
                .monto(monto)
                .plazoMeses(plazoMeses)
                .tipoPrestamo(tipoPrestamo)
                .build();
    }
}
