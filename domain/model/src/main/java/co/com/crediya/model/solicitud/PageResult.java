package co.com.crediya.model.solicitud;

import java.util.List;
import java.util.function.Function;

/** Una página de resultados. Garantiza sus invariantes al construirse. */
public record PageResult<T>(List<T> content, int page, int size, long totalElements) {

    public PageResult {
        if (content == null || page < 0 || size < 1 || totalElements < 0) {
            throw new IllegalArgumentException(
                    "Página inválida: contenido obligatorio, página >= 0, tamaño >= 1 y total >= 0.");
        }
        content = List.copyOf(content);
    }

    public int totalPages() {
        return (int) Math.ceil((double) totalElements / size);
    }

    public <R> PageResult<R> map(Function<T, R> mapper) {
        return new PageResult<>(content.stream().map(mapper).toList(), page, size, totalElements);
    }
}
