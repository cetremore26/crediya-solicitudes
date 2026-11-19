package co.com.crediya.usecase.solicitud;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import co.com.crediya.model.solicitud.DatosCliente;
import co.com.crediya.model.solicitud.EstadoSolicitud;
import co.com.crediya.model.solicitud.FiltroRevision;
import co.com.crediya.model.solicitud.Pagina;
import co.com.crediya.model.solicitud.Solicitud;
import co.com.crediya.model.solicitud.SolicitudRevision;
import co.com.crediya.model.solicitud.gateways.DatosClienteGateway;
import co.com.crediya.model.solicitud.gateways.SolicitudConsultaRepository;
import co.com.crediya.model.solicitud.exception.SolicitudInvalidaException;
import co.com.crediya.model.autenticacion.UsuarioAutenticado;
import co.com.crediya.model.autenticacion.Rol;
import co.com.crediya.model.solicitud.exception.AccesoDenegadoException;
import java.util.UUID;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ListarSolicitudesRevisionUseCaseTest {
    private static final String DOC_ANA = "1020304050";
    private static final String DOC_SIN_USUARIO = "999999";

    private final List<FiltroRevision> receivedFilters = new ArrayList<>();
    private final List<Solicitud> pending = new ArrayList<>();
    private final List<Solicitud> approved = new ArrayList<>();

    private final SolicitudConsultaRepository repository = new SolicitudConsultaRepository() {
        @Override
        public Pagina<Solicitud> buscarParaRevision(FiltroRevision filtro) {
            receivedFilters.add(filtro);
            return new Pagina<>(pending, filtro.pagina(), filtro.tamano(), pending.size());
        }

        @Override
        public List<Solicitud> buscarAprobadasPorDocumentos(Collection<String> documentos) {
            return approved;
        }
    };
    private final DatosClienteGateway clientes = documentos -> List.of(new DatosCliente(
            DOC_ANA, "Ana", "Pérez", "ana@example.com", new BigDecimal("4000000")));
    private static final UsuarioAutenticado ASESOR =
            new UsuarioAutenticado(UUID.randomUUID(), "asesor@crediya.com", Rol.ASESOR, null);
    private final ListarSolicitudesRevisionUseCase useCase =
            new ListarSolicitudesRevisionUseCase(repository, clientes);

    @Test
    void defaultsToAllReviewStatesAndNormalizesLoanType() {
        useCase.execute(null, " vehiculo ", 0, 10, ASESOR);

        FiltroRevision filtro = receivedFilters.get(0);
        assertEquals(
                EnumSet.of(EstadoSolicitud.PENDIENTE_DE_REVISION, EstadoSolicitud.RECHAZADA,
                        EstadoSolicitud.REVISION_MANUAL),
                filtro.estados());
        assertEquals("VEHICULO", filtro.tipoPrestamo());
    }

    @Test
    void returnsEmptyPageWithoutCallingUsersService() {
        Pagina<SolicitudRevision> page = useCase.execute(Set.of(), null, 0, 10, ASESOR);

        assertEquals(0, page.contenido().size());
        assertEquals(0, page.totalPaginas());
    }

    @Test
    void enrichesApplicationsWithClientDataAndMonthlyDebtOfApprovedLoans() {
        pending.add(solicitud(DOC_ANA, EstadoSolicitud.PENDIENTE_DE_REVISION, "1000000", 12, "0"));
        pending.add(solicitud(DOC_SIN_USUARIO, EstadoSolicitud.RECHAZADA, "500000", 6, "0"));
        approved.add(solicitud(DOC_ANA, EstadoSolicitud.APROBADA, "1200000", 12, "0"));
        approved.add(solicitud(DOC_ANA, EstadoSolicitud.APROBADA, "600000", 6, "0"));

        Pagina<SolicitudRevision> page = useCase.execute(null, null, 0, 10, ASESOR);

        SolicitudRevision ana = page.contenido().get(0);
        assertEquals("ana@example.com", ana.cliente().correoElectronico());
        assertEquals(new BigDecimal("200000.00"), ana.deudaMensualAprobadas());
        SolicitudRevision sinUsuario = page.contenido().get(1);
        assertNull(sinUsuario.cliente());
        assertEquals(BigDecimal.ZERO, sinUsuario.deudaMensualAprobadas());
    }

    @Test
    void rejectsInvalidPagingAndStates() {
        assertThrows(SolicitudInvalidaException.class, () -> useCase.execute(null, null, -1, 10, ASESOR));
        assertThrows(SolicitudInvalidaException.class, () -> useCase.execute(null, null, 0, 0, ASESOR));
        assertThrows(SolicitudInvalidaException.class, () -> useCase.execute(null, null, 0, 51, ASESOR));
        assertThrows(SolicitudInvalidaException.class,
                () -> useCase.execute(Set.of(EstadoSolicitud.APROBADA), null, 0, 10, ASESOR));
    }

    @Test
    void onlyAdvisorsCanReview() {
        for (Rol rol : List.of(Rol.ADMIN, Rol.CLIENTE)) {
            UsuarioAutenticado otro = new UsuarioAutenticado(UUID.randomUUID(), "x@crediya.com", rol, null);
            assertThrows(AccesoDenegadoException.class, () -> useCase.execute(null, null, 0, 10, otro));
        }
        assertThrows(AccesoDenegadoException.class, () -> useCase.execute(null, null, 0, 10, null));
    }

    @Test
    void toleratesDuplicatedClientDataFromTheUsersService() {
        DatosCliente ana = new DatosCliente(DOC_ANA, "Ana", "Pérez", "ana@example.com", BigDecimal.ONE);
        ListarSolicitudesRevisionUseCase withDuplicates =
                new ListarSolicitudesRevisionUseCase(repository, documentos -> List.of(ana, ana));

        assertDoesNotThrow(() -> withDuplicates.execute(null, null, 0, 10, ASESOR));
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
