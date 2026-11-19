package co.com.crediya.api.solicitud;

import co.com.crediya.model.solicitud.DatosCliente;
import co.com.crediya.model.solicitud.Solicitud;
import co.com.crediya.model.solicitud.SolicitudRevision;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.UUID;

public record SolicitudRevisionResponse(
        UUID id,
        BigDecimal monto,
        @JsonProperty("plazo_meses") Integer plazoMeses,
        @JsonProperty("correo_electronico") String correoElectronico,
        @JsonProperty("nombre_completo") String nombreCompleto,
        @JsonProperty("tipo_prestamo") String tipoPrestamo,
        @JsonProperty("tasa_interes") BigDecimal tasaInteres,
        @JsonProperty("estado_solicitud") String estadoSolicitud,
        @JsonProperty("estado_codigo") String estadoCodigo,
        @JsonProperty("salario_base") BigDecimal salarioBase,
        @JsonProperty("deuda_total_mensual_solicitudes_aprobadas") BigDecimal deudaTotalMensual) {

    public static SolicitudRevisionResponse desde(SolicitudRevision revision) {
        Solicitud solicitud = revision.solicitud();
        DatosCliente cliente = revision.cliente();
        return new SolicitudRevisionResponse(
                solicitud.getId(),
                solicitud.getMonto(),
                solicitud.getPlazoMeses(),
                cliente == null ? null : cliente.correoElectronico(),
                cliente == null ? null : cliente.nombreCompleto(),
                solicitud.getTipoPrestamo(),
                solicitud.getTasaInteres(),
                solicitud.getEstado().getDescripcion(),
                solicitud.getEstado().name(),
                cliente == null ? null : cliente.salarioBase(),
                revision.deudaMensualAprobadas());
    }
}
