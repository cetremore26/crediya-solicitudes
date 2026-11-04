package co.com.crediya.jpa.solicitud;

import co.com.crediya.model.solicitud.EstadoSolicitud;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.QueryByExampleExecutor;

public interface SolicitudDataRepository
        extends CrudRepository<SolicitudEntity, UUID>, QueryByExampleExecutor<SolicitudEntity> {

    @EntityGraph(attributePaths = "tipo")
    Page<SolicitudEntity> findByEstadoIn(Collection<EstadoSolicitud> estados, Pageable pageable);

    @EntityGraph(attributePaths = "tipo")
    Page<SolicitudEntity> findByEstadoInAndTipoPrestamo(
            Collection<EstadoSolicitud> estados, String tipoPrestamo, Pageable pageable);

    @EntityGraph(attributePaths = "tipo")
    List<SolicitudEntity> findByEstadoAndDocumentoIdentidadIn(
            EstadoSolicitud estado, Collection<String> documentos);
}
