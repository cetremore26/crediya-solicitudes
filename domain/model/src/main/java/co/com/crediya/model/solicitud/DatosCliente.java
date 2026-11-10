package co.com.crediya.model.solicitud;

import java.math.BigDecimal;
import java.util.stream.Stream;

public record DatosCliente(
        String documentoIdentidad,
        String nombres,
        String apellidos,
        String correoElectronico,
        BigDecimal salarioBase) {

    /** Nombres y apellidos separados por un espacio, o {@code null} si no hay ninguno. */
    public String nombreCompleto() {
        return Stream.of(nombres, apellidos)
                .filter(part -> part != null && !part.isBlank())
                .reduce((names, lastNames) -> names + " " + lastNames)
                .orElse(null);
    }
}
