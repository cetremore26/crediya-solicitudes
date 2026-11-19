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
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Persistable;

/**
 * Implementa {@link Persistable}: el id lo asigna la aplicación, así que sin esto Spring Data
 * no sabría que la entidad es nueva y haría un SELECT (merge) antes de cada INSERT.
 */
@Entity
@Table(name = "solicitudes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitudEntity implements Persistable<UUID> {
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

    /** Vista de solo lectura del tipo de préstamo, para exponer su tasa de interés. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_prestamo", referencedColumnName = "nombre",
            insertable = false, updatable = false)
    private TipoPrestamoEntity tipo;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 40)
    private EstadoSolicitud estado;

    /** Lo llena el valor por defecto de la base de datos al insertar. */
    @Column(name = "fecha_creacion", insertable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Transient
    @Builder.Default
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private boolean nueva = true;

    @Override
    public boolean isNew() {
        return nueva;
    }

    @PostLoad
    @PostPersist
    void marcarComoPersistida() {
        nueva = false;
    }
}
