-- Tipos de préstamo iniciales. Solo inserta los que falten: no pisa tasas ya ajustadas.
MERGE INTO tipos_prestamo t
USING (VALUES
    ('LIBRE_INVERSION', 18.00),
    ('VEHICULO', 14.00),
    ('VIVIENDA', 11.00),
    ('EDUCACION', 9.00)
) AS v (nombre, tasa_interes)
ON t.nombre = v.nombre
WHEN NOT MATCHED THEN
    INSERT (nombre, tasa_interes) VALUES (v.nombre, v.tasa_interes);
