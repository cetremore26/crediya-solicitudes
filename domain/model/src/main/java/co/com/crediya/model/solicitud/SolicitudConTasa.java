package co.com.crediya.model.solicitud;

import java.math.BigDecimal;

/**
 * Una solicitud leída junto con la tasa de interés anual (%) de su tipo de préstamo.
 * Siempre trae la tasa, así que la cuota mensual se puede calcular sin condiciones previas.
 */
public record SolicitudConTasa(Solicitud solicitud, BigDecimal tasaInteres) {

    public SolicitudConTasa {
        if (solicitud == null || tasaInteres == null || tasaInteres.signum() < 0) {
            throw new IllegalArgumentException(
                    "Una solicitud consultada necesita la solicitud y una tasa de interés no negativa.");
        }
    }

    /** Cuota mensual según la tasa de interés de su tipo de préstamo. */
    public BigDecimal cuotaMensual() {
        return CalculadoraCuotaMensual.calcular(solicitud.getMonto(), solicitud.getPlazoMeses(), tasaInteres);
    }

    public String documentoIdentidad() {
        return solicitud.getDocumentoIdentidad();
    }
}
