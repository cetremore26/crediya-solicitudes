CREATE TABLE IF NOT EXISTS tipos_prestamo (
    nombre VARCHAR(50) PRIMARY KEY
);

MERGE INTO tipos_prestamo (nombre) KEY (nombre) VALUES
    ('LIBRE_INVERSION'),
    ('VEHICULO'),
    ('VIVIENDA'),
    ('EDUCACION');

CREATE TABLE IF NOT EXISTS solicitudes (
    id UUID PRIMARY KEY,
    documento_identidad VARCHAR(15) NOT NULL,
    monto DECIMAL(17, 2) NOT NULL,
    plazo_meses INT NOT NULL,
    tipo_prestamo VARCHAR(50) NOT NULL,
    estado VARCHAR(40) NOT NULL,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_solicitudes_tipo_prestamo FOREIGN KEY (tipo_prestamo)
        REFERENCES tipos_prestamo (nombre)
);
