package co.com.crediya.model.solicitud;

import co.com.crediya.model.solicitud.exception.SolicitudInvalidaException;
import java.math.BigDecimal;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Solicitud de préstamo. Además de los datos, conoce sus propias reglas:
 * cómo se normaliza ({@link #normalizado()}) y cuándo es válida ({@link #validar()}).
 * La cuota mensual depende de la tasa del tipo de préstamo y se calcula en
 * {@link SolicitudConTasa#cuotaMensual()}. Es inmutable: los cambios se hacen con {@code toBuilder()}.
 */
@Getter
@AllArgsConstructor
@Builder(toBuilder = true)
public class Solicitud {
    private static final Pattern PATRON_DOCUMENTO = Pattern.compile("^\\d{5,15}$");

    private UUID id;
    private String documentoIdentidad;
    private BigDecimal monto;
    private Integer plazoMeses;
    private String tipoPrestamo;
    private EstadoSolicitud estado;

    /**
     * Devuelve una copia con los datos limpios: documento sin espacios y tipo de préstamo
     * sin espacios y en mayúsculas.
     */
    public Solicitud normalizado() {
        return toBuilder()
                .documentoIdentidad(documentoIdentidad == null ? null : documentoIdentidad.trim())
                .tipoPrestamo(normalizarTipoPrestamo(tipoPrestamo))
                .build();
    }

    /**
     * Verifica que la solicitud cumpla las reglas del dominio. Se debe llamar sobre
     * una solicitud ya normalizada.
     *
     * @throws SolicitudInvalidaException con el primer dato inválido encontrado
     */
    public void validar() {
        if (documentoIdentidad == null || !PATRON_DOCUMENTO.matcher(documentoIdentidad).matches()) {
            throw new SolicitudInvalidaException(
                    "El documento de identidad debe contener entre 5 y 15 dígitos.");
        }
        if (monto == null || monto.signum() <= 0) {
            throw new SolicitudInvalidaException("El monto debe ser mayor a cero.");
        }
        if (plazoMeses == null || plazoMeses <= 0) {
            throw new SolicitudInvalidaException("El plazo debe ser mayor a cero meses.");
        }
        if (tipoPrestamo == null) {
            throw new SolicitudInvalidaException("El tipo de préstamo es obligatorio.");
        }
    }

    /** Tipo de préstamo sin espacios y en mayúsculas, o {@code null} si viene vacío. */
    public static String normalizarTipoPrestamo(String tipoPrestamo) {
        return tipoPrestamo == null || tipoPrestamo.isBlank()
                ? null
                : tipoPrestamo.trim().toUpperCase(Locale.ROOT);
    }
}
