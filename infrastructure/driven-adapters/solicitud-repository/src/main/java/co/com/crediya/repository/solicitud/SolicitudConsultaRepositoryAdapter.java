package co.com.crediya.repository.solicitud;

import co.com.crediya.model.solicitud.EstadoSolicitud;
import co.com.crediya.model.solicitud.FiltroRevision;
import co.com.crediya.model.solicitud.PageResult;
import co.com.crediya.model.solicitud.Solicitud;
import co.com.crediya.model.solicitud.gateways.SolicitudConsultaRepository;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class SolicitudConsultaRepositoryAdapter implements SolicitudConsultaRepository {
    private static final Logger LOGGER =
            LoggerFactory.getLogger(SolicitudConsultaRepositoryAdapter.class);
    private static final String SELECT_SOLICITUDES = """
            SELECT s.id, s.documento_identidad, s.monto, s.plazo_meses, s.tipo_prestamo,
                   s.estado, t.tasa_interes
            FROM solicitudes s
            JOIN tipos_prestamo t ON t.nombre = s.tipo_prestamo
            """;
    private static final String COUNT_SOLICITUDES = """
            SELECT COUNT(*) FROM solicitudes s
            """;
    private static final String ORDER_BY_OLDEST = " ORDER BY s.fecha_creacion, s.id";

    private static final RowMapper<Solicitud> SOLICITUD_MAPPER = (rs, rowNum) -> Solicitud.builder()
            .id(rs.getObject("id", UUID.class))
            .documentoIdentidad(rs.getString("documento_identidad"))
            .monto(rs.getBigDecimal("monto"))
            .plazoMeses(rs.getInt("plazo_meses"))
            .tipoPrestamo(rs.getString("tipo_prestamo"))
            .estado(EstadoSolicitud.valueOf(rs.getString("estado")))
            .tasaInteres(rs.getBigDecimal("tasa_interes"))
            .build();

    private final JdbcTemplate jdbcTemplate;

    public SolicitudConsultaRepositoryAdapter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public PageResult<Solicitud> findForReview(FiltroRevision filtro) {
        List<Object> parameters = new ArrayList<>();
        String where = buildWhere(filtro, parameters);

        Long total = jdbcTemplate.queryForObject(
                COUNT_SOLICITUDES + where, Long.class, parameters.toArray());

        List<Object> pageParameters = new ArrayList<>(parameters);
        pageParameters.add(filtro.size());
        pageParameters.add((long) filtro.page() * filtro.size());
        List<Solicitud> content = jdbcTemplate.query(
                SELECT_SOLICITUDES + where + ORDER_BY_OLDEST + " LIMIT ? OFFSET ?",
                SOLICITUD_MAPPER,
                pageParameters.toArray());

        LOGGER.info("Consulta de revisión: {} de {} solicitudes.", content.size(), total);
        return new PageResult<>(content, filtro.page(), filtro.size(), total == null ? 0 : total);
    }

    @Override
    public List<Solicitud> findApprovedByDocumentos(Collection<String> documentos) {
        if (documentos.isEmpty()) {
            return List.of();
        }
        List<Object> parameters = new ArrayList<>();
        parameters.add(EstadoSolicitud.APROBADA.name());
        parameters.addAll(documentos);
        return jdbcTemplate.query(
                SELECT_SOLICITUDES + " WHERE s.estado = ? AND s.documento_identidad IN ("
                        + placeholders(documentos.size()) + ")",
                SOLICITUD_MAPPER,
                parameters.toArray());
    }

    private String buildWhere(FiltroRevision filtro, List<Object> parameters) {
        StringBuilder where = new StringBuilder(" WHERE s.estado IN (")
                .append(placeholders(filtro.estados().size()))
                .append(")");
        filtro.estados().forEach(estado -> parameters.add(estado.name()));
        if (filtro.tipoPrestamo() != null) {
            where.append(" AND s.tipo_prestamo = ?");
            parameters.add(filtro.tipoPrestamo());
        }
        return where.toString();
    }

    private String placeholders(int count) {
        return String.join(", ", java.util.Collections.nCopies(count, "?"));
    }
}
