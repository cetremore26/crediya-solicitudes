CREATE TABLE IF NOT EXISTS tipos_prestamo (
    nombre VARCHAR(50) PRIMARY KEY
);

ALTER TABLE tipos_prestamo ADD COLUMN IF NOT EXISTS tasa_interes DECIMAL(5, 2) DEFAULT 0 NOT NULL;

MERGE INTO tipos_prestamo (nombre, tasa_interes) KEY (nombre) VALUES
    ('LIBRE_INVERSION', 18.00),
    ('VEHICULO', 14.00),
    ('VIVIENDA', 11.00),
    ('EDUCACION', 9.00);

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

CREATE INDEX IF NOT EXISTS idx_solicitudes_estado ON solicitudes (estado, fecha_creacion);
CREATE INDEX IF NOT EXISTS idx_solicitudes_documento ON solicitudes (documento_identidad);
