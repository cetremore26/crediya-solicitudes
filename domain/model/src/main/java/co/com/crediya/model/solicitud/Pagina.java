package co.com.crediya.model.solicitud;

import java.util.List;
import java.util.function.Function;

/** Una página de resultados. Garantiza sus invariantes al construirse. */
public record Pagina<T>(List<T> contenido, int numero, int tamano, long totalElementos) {

    public Pagina {
        if (contenido == null || numero < 0 || tamano < 1 || totalElementos < 0) {
            throw new IllegalArgumentException(
                    "Página inválida: contenido obligatorio, página >= 0, tamaño >= 1 y total >= 0.");
        }
        contenido = List.copyOf(contenido);
    }

    public int totalPaginas() {
        return (int) Math.ceil((double) totalElementos / tamano);
    }

    /** La misma página con cada elemento transformado. */
    public <R> Pagina<R> map(Function<T, R> transformacion) {
        return new Pagina<>(contenido.stream().map(transformacion).toList(), numero, tamano, totalElementos);
    }
}
