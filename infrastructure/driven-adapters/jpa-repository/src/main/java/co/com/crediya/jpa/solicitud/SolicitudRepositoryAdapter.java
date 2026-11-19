package co.com.crediya.jpa.solicitud;

import co.com.crediya.jpa.helper.AdapterOperations;
import co.com.crediya.model.solicitud.EstadoSolicitud;
import co.com.crediya.model.solicitud.FiltroRevision;
import co.com.crediya.model.solicitud.Pagina;
import co.com.crediya.model.solicitud.Solicitud;
import co.com.crediya.model.solicitud.SolicitudConTasa;
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
    private static final Sort MAS_ANTIGUAS_PRIMERO = Sort.by("fechaCreacion", "id");

    public SolicitudRepositoryAdapter(SolicitudDataRepository repository, ObjectMapper mapper) {
        super(repository, mapper, SolicitudRepositoryAdapter::aDominio);
    }

    @Override
    public Solicitud guardar(Solicitud solicitud) {
        UUID id = solicitud.getId() == null ? UUID.randomUUID() : solicitud.getId();
        Solicitud guardada = super.save(solicitud.toBuilder().id(id).build());
        LOGGER.info("Solicitud guardada con id {}.", id);
        return guardada;
    }

    @Override
    public Pagina<SolicitudConTasa> buscarParaRevision(FiltroRevision filtro) {
        Pageable pageable = PageRequest.of(filtro.pagina(), filtro.tamano(), MAS_ANTIGUAS_PRIMERO);
        Page<SolicitudEntity> resultado = filtro.tipoPrestamo() == null
                ? repository.findByEstadoIn(filtro.estados(), pageable)
                : repository.findByEstadoInAndTipoPrestamo(
                        filtro.estados(), filtro.tipoPrestamo(), pageable);

        LOGGER.info("Consulta de revisión: {} de {} solicitudes.",
                resultado.getNumberOfElements(), resultado.getTotalElements());
        return new Pagina<>(
                aConsulta(resultado.getContent()), filtro.pagina(), filtro.tamano(), resultado.getTotalElements());
    }

    @Override
    public List<SolicitudConTasa> buscarAprobadasPorDocumentos(Collection<String> documentos) {
        if (documentos.isEmpty()) {
            return List.of();
        }
        return aConsulta(repository.findByEstadoAndDocumentoIdentidadIn(
                EstadoSolicitud.APROBADA, documentos));
    }

    /** Mapeo explícito: el modelo de dominio es inmutable (builder) y no tiene anotaciones JPA. */
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

    private static Solicitud aDominio(SolicitudEntity entity) {
        return Solicitud.builder()
                .id(entity.getId())
                .documentoIdentidad(entity.getDocumentoIdentidad())
                .monto(entity.getMonto())
                .plazoMeses(entity.getPlazoMeses())
                .tipoPrestamo(entity.getTipoPrestamo())
                .estado(entity.getEstado())
                .build();
    }

    /** La tasa sale del tipo de préstamo, que la llave foránea garantiza que existe. */
    private static List<SolicitudConTasa> aConsulta(List<SolicitudEntity> entities) {
        return entities.stream()
                .map(entity -> new SolicitudConTasa(aDominio(entity), entity.getTipo().getTasaInteres()))
                .toList();
    }
}
