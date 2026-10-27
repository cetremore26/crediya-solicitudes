package co.com.crediya.model.solicitud;

public enum EstadoSolicitud {
    PENDIENTE_DE_REVISION("Pendiente de revisión"),
    REVISION_MANUAL("Revisión manual"),
    RECHAZADA("Rechazada"),
    APROBADA("Aprobada");

    private final String descripcion;

    EstadoSolicitud(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
