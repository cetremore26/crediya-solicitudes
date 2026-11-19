package co.com.crediya.model.autenticacion;

import java.util.UUID;

public record UsuarioAutenticado(
        UUID idUsuario, String correoElectronico, Rol rol, String documentoIdentidad) {

    /** Un usuario solo actúa a nombre de un documento si es CLIENTE y ese documento es el suyo. */
    public boolean esClienteConDocumento(String documento) {
        return rol == Rol.CLIENTE && documento != null && documento.equals(documentoIdentidad);
    }

    /** Solo un ASESOR revisa manualmente las solicitudes (HU-4). */
    public boolean puedeRevisarSolicitudes() {
        return rol == Rol.ASESOR;
    }
}
