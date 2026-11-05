package co.com.crediya.model.solicitud;

import java.math.BigDecimal;

public record DatosCliente(
        String documentoIdentidad,
        String nombres,
        String apellidos,
        String correoElectronico,
        BigDecimal salarioBase) {
}
