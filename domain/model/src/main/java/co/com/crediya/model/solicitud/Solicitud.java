package co.com.crediya.model.solicitud;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder(toBuilder = true)
public class Solicitud {
    private UUID id;
    private String documentoIdentidad;
    private BigDecimal monto;
    private Integer plazoMeses;
    private String tipoPrestamo;
    private EstadoSolicitud estado;
    /** Annual interest rate (%) of the loan type; only filled when reading applications. */
    private BigDecimal tasaInteres;
}
