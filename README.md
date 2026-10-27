# crediya-solicitudes

Microservicio de solicitudes de préstamo de CrediYa (arquitectura hexagonal, Spring Boot).

## Endpoints
- `POST /api/v1/solicitud` registra una solicitud con estado inicial "Pendiente de revisión".
- Swagger UI: `/swagger-ui.html`.

## Configuración
- `SERVER_PORT` (8081 por defecto)
- `USUARIOS_SERVICE_URL` URL del microservicio de autenticación (http://localhost:8080 por defecto)
- `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`
