package co.com.crediya.model.solicitud;

import co.com.crediya.model.solicitud.exception.SolicitudValidationException;
import java.math.BigDecimal;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Solicitud de préstamo. Además de los datos, conoce sus propias reglas:
 * cómo se normaliza ({@link #normalized()}) y cuándo es válida ({@link #validate()}).
 */
@Getter
@Setter
@AllArgsConstructor
@Builder(toBuilder = true)
public class Solicitud {
    private static final Pattern DOCUMENTO_PATTERN = Pattern.compile("^\\d{5,15}$");

    private UUID id;
    private String documentoIdentidad;
    private BigDecimal monto;
    private Integer plazoMeses;
    private String tipoPrestamo;
    private EstadoSolicitud estado;
    /** Annual interest rate (%) of the loan type; only filled when reading applications. */
    private BigDecimal tasaInteres;

    /**
     * Devuelve una copia con los datos limpios: documento sin espacios y tipo de préstamo
     * sin espacios y en mayúsculas.
     */
    public Solicitud normalized() {
        return toBuilder()
                .documentoIdentidad(documentoIdentidad == null ? null : documentoIdentidad.trim())
                .tipoPrestamo(normalizeTipoPrestamo(tipoPrestamo))
                .build();
    }

    /**
     * Verifica que la solicitud cumpla las reglas del dominio. Se debe llamar sobre
     * una solicitud ya normalizada.
     *
     * @throws SolicitudValidationException con el primer dato inválido encontrado
     */
    public void validate() {
        if (documentoIdentidad == null || !DOCUMENTO_PATTERN.matcher(documentoIdentidad).matches()) {
            throw new SolicitudValidationException(
                    "El documento de identidad debe contener entre 5 y 15 dígitos.");
        }
        if (monto == null || monto.signum() <= 0) {
            throw new SolicitudValidationException("El monto debe ser mayor a cero.");
        }
        if (plazoMeses == null || plazoMeses <= 0) {
            throw new SolicitudValidationException("El plazo debe ser mayor a cero meses.");
        }
        if (tipoPrestamo == null) {
            throw new SolicitudValidationException("El tipo de préstamo es obligatorio.");
        }
    }

    /** Tipo de préstamo sin espacios y en mayúsculas, o {@code null} si viene vacío. */
    public static String normalizeTipoPrestamo(String tipoPrestamo) {
        return tipoPrestamo == null || tipoPrestamo.isBlank()
                ? null
                : tipoPrestamo.trim().toUpperCase(Locale.ROOT);
    }
}
