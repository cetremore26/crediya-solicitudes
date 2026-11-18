package co.com.crediya.api.solicitud;

import co.com.crediya.api.ApiErrorResponse;
import co.com.crediya.api.PageResponse;
import co.com.crediya.model.auth.AuthenticatedUser;
import co.com.crediya.model.solicitud.EstadoSolicitud;
import co.com.crediya.model.solicitud.PageResult;
import co.com.crediya.model.solicitud.Solicitud;
import co.com.crediya.model.solicitud.SolicitudRevision;
import co.com.crediya.usecase.solicitud.ListSolicitudesRevisionUseCase;
import co.com.crediya.usecase.solicitud.RegisterSolicitudUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Solicitudes", description = "Gestión de solicitudes de préstamo")
@RequestMapping("/api/v1/solicitud")
public class SolicitudController {
    private static final Logger LOGGER = LoggerFactory.getLogger(SolicitudController.class);

    private final RegisterSolicitudUseCase registerSolicitudUseCase;
    private final ListSolicitudesRevisionUseCase listSolicitudesRevisionUseCase;

    public SolicitudController(
            RegisterSolicitudUseCase registerSolicitudUseCase,
            ListSolicitudesRevisionUseCase listSolicitudesRevisionUseCase) {
        this.registerSolicitudUseCase = registerSolicitudUseCase;
        this.listSolicitudesRevisionUseCase = listSolicitudesRevisionUseCase;
    }

    @PostMapping
    @Operation(summary = "Registrar una solicitud de préstamo",
            description = "Registra una solicitud con estado inicial 'Pendiente de revisión'.")
    @ApiResponse(responseCode = "201", description = "Solicitud registrada exitosamente")
    @ApiResponse(responseCode = "400", description = "Datos inválidos o tipo de préstamo inexistente",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "El cliente no existe",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "503", description = "No fue posible verificar al cliente",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "No autenticado")
    @ApiResponse(responseCode = "403", description = "Solo clientes, y solo a su nombre")
    public ResponseEntity<SolicitudResponse> register(
            @RequestBody SolicitudRequest request, Authentication authentication) {
        LOGGER.info("Solicitud de préstamo recibida.");
        Solicitud registered = registerSolicitudUseCase.execute(
                request.toDomain(), (AuthenticatedUser) authentication.getPrincipal());
        LOGGER.info("Solicitud de préstamo registrada con id {}.", registered.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(SolicitudResponse.from(registered));
    }

    @GetMapping
    @Operation(summary = "Listar solicitudes para revisión manual",
            description = "Solo ASESOR. Lista paginada de solicitudes pendientes de revisión, "
                    + "rechazadas o en revisión manual, filtrable por estado y tipo de préstamo.")
    @ApiResponse(responseCode = "200", description = "Página de solicitudes")
    @ApiResponse(responseCode = "400", description = "Filtros o paginación inválidos",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "No autenticado")
    @ApiResponse(responseCode = "403", description = "Solo el rol ASESOR puede consultar")
    @ApiResponse(responseCode = "503", description = "No fue posible obtener los datos de los clientes",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public ResponseEntity<PageResponse<SolicitudRevisionResponse>> listForReview(
            @RequestParam(value = "estado", required = false) List<EstadoSolicitud> estados,
            @RequestParam(value = "tipo_prestamo", required = false) String tipoPrestamo,
            @RequestParam(value = "pagina", defaultValue = "0") int pagina,
            @RequestParam(value = "tamano", defaultValue = "10") int tamano) {
        LOGGER.info("Consulta de solicitudes para revisión: pagina={}, tamano={}.", pagina, tamano);
        PageResult<SolicitudRevision> page =
                listSolicitudesRevisionUseCase.execute(estados, tipoPrestamo, pagina, tamano);
        return ResponseEntity.ok(PageResponse.from(page, SolicitudRevisionResponse::from));
    }
}
