package co.com.crediya.jpa.solicitud;

import co.com.crediya.jpa.helper.AdapterOperations;
import co.com.crediya.model.solicitud.EstadoSolicitud;
import co.com.crediya.model.solicitud.FiltroRevision;
import co.com.crediya.model.solicitud.PageResult;
import co.com.crediya.model.solicitud.Solicitud;
import co.com.crediya.model.solicitud.gateways.SolicitudConsultaRepository;
import co.com.crediya.model.solicitud.gateways.SolicitudRepository;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.reactivecommons.utils.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

@Repository
public class SolicitudRepositoryAdapter
        extends AdapterOperations<Solicitud, SolicitudEntity, UUID, SolicitudDataRepository>
        implements SolicitudRepository, SolicitudConsultaRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger(SolicitudRepositoryAdapter.class);
    private static final Sort OLDEST_FIRST = Sort.by("fechaCreacion", "id");

    public SolicitudRepositoryAdapter(SolicitudDataRepository repository, ObjectMapper mapper) {
        super(repository, mapper, SolicitudRepositoryAdapter::toDomain);
    }

    @Override
    public Solicitud save(Solicitud solicitud) {
        UUID id = solicitud.getId() == null ? UUID.randomUUID() : solicitud.getId();
        Solicitud saved = super.save(solicitud.toBuilder().id(id).build());
        LOGGER.info("Solicitud guardada con id {}.", id);
        return saved;
    }

    @Override
    public PageResult<Solicitud> findForReview(FiltroRevision filtro) {
        Pageable pageable = PageRequest.of(filtro.page(), filtro.size(), OLDEST_FIRST);
        Page<SolicitudEntity> page = filtro.tipoPrestamo() == null
                ? repository.findByEstadoIn(filtro.estados(), pageable)
                : repository.findByEstadoInAndTipoPrestamo(
                        filtro.estados(), filtro.tipoPrestamo(), pageable);

        LOGGER.info("Consulta de revisión: {} de {} solicitudes.",
                page.getNumberOfElements(), page.getTotalElements());
        return new PageResult<>(toList(page), filtro.page(), filtro.size(), page.getTotalElements());
    }

    @Override
    public List<Solicitud> findApprovedByDocumentos(Collection<String> documentos) {
        if (documentos.isEmpty()) {
            return List.of();
        }
        return toList(repository.findByEstadoAndDocumentoIdentidadIn(
                EstadoSolicitud.APROBADA, documentos));
    }

    /** Explicit mapping: the domain model is immutable-by-builder and has no JPA annotations. */
    @Override
    protected SolicitudEntity toData(Solicitud solicitud) {
        return SolicitudEntity.builder()
                .id(solicitud.getId())
                .documentoIdentidad(solicitud.getDocumentoIdentidad())
                .monto(solicitud.getMonto())
                .plazoMeses(solicitud.getPlazoMeses())
                .tipoPrestamo(solicitud.getTipoPrestamo())
                .estado(solicitud.getEstado())
                .build();
    }

    private static Solicitud toDomain(SolicitudEntity entity) {
        return Solicitud.builder()
                .id(entity.getId())
                .documentoIdentidad(entity.getDocumentoIdentidad())
                .monto(entity.getMonto())
                .plazoMeses(entity.getPlazoMeses())
                .tipoPrestamo(entity.getTipoPrestamo())
                .estado(entity.getEstado())
                .tasaInteres(entity.getTipo() == null ? null : entity.getTipo().getTasaInteres())
                .build();
    }
}
