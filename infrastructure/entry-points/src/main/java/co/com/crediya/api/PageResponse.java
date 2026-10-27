package co.com.crediya.api;

import co.com.crediya.model.solicitud.PageResult;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.function.Function;

public record PageResponse<T>(
        List<T> contenido,
        int pagina,
        int tamano,
        @JsonProperty("total_elementos") long totalElementos,
        @JsonProperty("total_paginas") int totalPaginas) {

    public static <S, T> PageResponse<T> from(PageResult<S> page, Function<S, T> mapper) {
        return new PageResponse<>(
                page.content().stream().map(mapper).toList(),
                page.page(),
                page.size(),
                page.totalElements(),
                page.totalPages());
    }
}
