package co.com.crediya.usecase.solicitud;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import co.com.crediya.model.solicitud.DatosCliente;
import co.com.crediya.model.solicitud.EstadoSolicitud;
import co.com.crediya.model.solicitud.FiltroRevision;
import co.com.crediya.model.solicitud.PageResult;
import co.com.crediya.model.solicitud.Solicitud;
import co.com.crediya.model.solicitud.SolicitudRevision;
import co.com.crediya.model.solicitud.gateways.DatosClienteGateway;
import co.com.crediya.model.solicitud.gateways.SolicitudConsultaRepository;
import co.com.crediya.model.solicitud.exception.SolicitudValidationException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ListSolicitudesRevisionUseCaseTest {
    private static final String DOC_ANA = "1020304050";
    private static final String DOC_SIN_USUARIO = "999999";

    private final List<FiltroRevision> receivedFilters = new ArrayList<>();
    private final List<Solicitud> pending = new ArrayList<>();
    private final List<Solicitud> approved = new ArrayList<>();

    private final SolicitudConsultaRepository repository = new SolicitudConsultaRepository() {
        @Override
        public PageResult<Solicitud> findForReview(FiltroRevision filtro) {
            receivedFilters.add(filtro);
            return new PageResult<>(pending, filtro.page(), filtro.size(), pending.size());
        }

        @Override
        public List<Solicitud> findApprovedByDocumentos(Collection<String> documentos) {
            return approved;
        }
    };
    private final DatosClienteGateway clientes = documentos -> List.of(new DatosCliente(
            DOC_ANA, "Ana", "Pérez", "ana@example.com", new BigDecimal("4000000")));
    private final ListSolicitudesRevisionUseCase useCase =
            new ListSolicitudesRevisionUseCase(repository, clientes);

    @Test
    void defaultsToAllReviewStatesAndNormalizesLoanType() {
        useCase.execute(null, " vehiculo ", 0, 10);

        FiltroRevision filtro = receivedFilters.get(0);
        assertEquals(
                EnumSet.of(EstadoSolicitud.PENDIENTE_DE_REVISION, EstadoSolicitud.RECHAZADA,
                        EstadoSolicitud.REVISION_MANUAL),
                filtro.estados());
        assertEquals("VEHICULO", filtro.tipoPrestamo());
    }

    @Test
    void returnsEmptyPageWithoutCallingUsersService() {
        PageResult<SolicitudRevision> page = useCase.execute(Set.of(), null, 0, 10);

        assertEquals(0, page.content().size());
        assertEquals(0, page.totalPages());
    }

    @Test
    void enrichesApplicationsWithClientDataAndMonthlyDebtOfApprovedLoans() {
        pending.add(solicitud(DOC_ANA, EstadoSolicitud.PENDIENTE_DE_REVISION, "1000000", 12, "0"));
        pending.add(solicitud(DOC_SIN_USUARIO, EstadoSolicitud.RECHAZADA, "500000", 6, "0"));
        approved.add(solicitud(DOC_ANA, EstadoSolicitud.APROBADA, "1200000", 12, "0"));
        approved.add(solicitud(DOC_ANA, EstadoSolicitud.APROBADA, "600000", 6, "0"));

        PageResult<SolicitudRevision> page = useCase.execute(null, null, 0, 10);

        SolicitudRevision ana = page.content().get(0);
        assertEquals("ana@example.com", ana.cliente().correoElectronico());
        assertEquals(new BigDecimal("200000.00"), ana.deudaMensualAprobadas());
        SolicitudRevision sinUsuario = page.content().get(1);
        assertNull(sinUsuario.cliente());
        assertEquals(BigDecimal.ZERO, sinUsuario.deudaMensualAprobadas());
    }

    @Test
    void rejectsInvalidPagingAndStates() {
        assertThrows(SolicitudValidationException.class, () -> useCase.execute(null, null, -1, 10));
        assertThrows(SolicitudValidationException.class, () -> useCase.execute(null, null, 0, 0));
        assertThrows(SolicitudValidationException.class, () -> useCase.execute(null, null, 0, 51));
        assertThrows(SolicitudValidationException.class,
                () -> useCase.execute(Set.of(EstadoSolicitud.APROBADA), null, 0, 10));
    }

    private Solicitud solicitud(
            String documento, EstadoSolicitud estado, String monto, int plazo, String tasa) {
        return Solicitud.builder()
                .documentoIdentidad(documento)
                .estado(estado)
                .monto(new BigDecimal(monto))
                .plazoMeses(plazo)
                .tipoPrestamo("VEHICULO")
                .tasaInteres(new BigDecimal(tasa))
                .build();
    }
}
