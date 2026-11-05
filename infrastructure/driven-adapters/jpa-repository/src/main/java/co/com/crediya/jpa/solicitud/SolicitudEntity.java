package co.com.crediya.jpa.solicitud;

import co.com.crediya.jpa.tipoprestamo.TipoPrestamoEntity;
import co.com.crediya.model.solicitud.EstadoSolicitud;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "solicitudes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitudEntity {
    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "documento_identidad", nullable = false, length = 15)
    private String documentoIdentidad;

    @Column(name = "monto", nullable = false, precision = 17, scale = 2)
    private BigDecimal monto;

    @Column(name = "plazo_meses", nullable = false)
    private Integer plazoMeses;

    @Column(name = "tipo_prestamo", nullable = false, length = 50)
    private String tipoPrestamo;

    /** Read-only view of the loan type, used to expose its interest rate. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_prestamo", referencedColumnName = "nombre",
            insertable = false, updatable = false)
    private TipoPrestamoEntity tipo;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 40)
    private EstadoSolicitud estado;

    /** Filled by the database default on insert. */
    @Column(name = "fecha_creacion", insertable = false, updatable = false)
    private LocalDateTime fechaCreacion;
}
