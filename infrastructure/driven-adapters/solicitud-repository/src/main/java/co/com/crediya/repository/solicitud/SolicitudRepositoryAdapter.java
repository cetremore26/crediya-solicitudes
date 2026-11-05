package co.com.crediya.repository.solicitud;

import co.com.crediya.model.solicitud.Solicitud;
import co.com.crediya.model.solicitud.gateways.SolicitudRepository;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class SolicitudRepositoryAdapter implements SolicitudRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger(SolicitudRepositoryAdapter.class);
    private static final String INSERT_SOLICITUD = """
            INSERT INTO solicitudes (
                id, documento_identidad, monto, plazo_meses, tipo_prestamo, estado
            ) VALUES (?, ?, ?, ?, ?, ?)
            """;

    private final JdbcTemplate jdbcTemplate;

    public SolicitudRepositoryAdapter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public Solicitud save(Solicitud solicitud) {
        UUID id = solicitud.getId() == null ? UUID.randomUUID() : solicitud.getId();

        jdbcTemplate.update(
                INSERT_SOLICITUD,
                id,
                solicitud.getDocumentoIdentidad(),
                solicitud.getMonto(),
                solicitud.getPlazoMeses(),
                solicitud.getTipoPrestamo(),
                solicitud.getEstado().name());

        LOGGER.info("Solicitud guardada con id {}.", id);
        return solicitud.toBuilder().id(id).build();
    }
}
