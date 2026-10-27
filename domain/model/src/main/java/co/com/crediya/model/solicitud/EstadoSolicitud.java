package co.com.crediya.model.solicitud;

public enum EstadoSolicitud {
    PENDIENTE_DE_REVISION("Pendiente de revisión");

    private final String descripcion;

    EstadoSolicitud(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
