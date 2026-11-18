package co.com.crediya.client.usuario;

import java.io.IOException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Reenvía al servicio de usuarios el header Authorization de la petición que se está
 * atendiendo, para que las llamadas salgan con la identidad de quien las originó.
 * Es el único lugar del cliente que conoce la petición HTTP entrante.
 */
class AuthorizationPropagationInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(
            HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {
        String authorization = currentAuthorization();
        if (authorization != null) {
            request.getHeaders().set(HttpHeaders.AUTHORIZATION, authorization);
        }
        return execution.execute(request, body);
    }

    private static String currentAuthorization() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            return attributes.getRequest().getHeader(HttpHeaders.AUTHORIZATION);
        }
        return null;
    }
}
