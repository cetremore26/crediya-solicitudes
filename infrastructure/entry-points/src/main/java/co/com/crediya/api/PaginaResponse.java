package co.com.crediya.api;

import co.com.crediya.model.solicitud.Pagina;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.function.Function;

public record PaginaResponse<T>(
        List<T> contenido,
        int pagina,
        int tamano,
        @JsonProperty("total_elementos") long totalElementos,
        @JsonProperty("total_paginas") int totalPaginas) {

    public static <S, T> PaginaResponse<T> desde(Pagina<S> pagina, Function<S, T> transformacion) {
        return new PaginaResponse<>(
                pagina.contenido().stream().map(transformacion).toList(),
                pagina.numero(),
                pagina.tamano(),
                pagina.totalElementos(),
                pagina.totalPaginas());
    }
}
