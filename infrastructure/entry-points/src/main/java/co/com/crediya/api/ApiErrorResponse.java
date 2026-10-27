package co.com.crediya.api;

public record ApiErrorResponse(int status, String codigo, String mensaje) {
}
