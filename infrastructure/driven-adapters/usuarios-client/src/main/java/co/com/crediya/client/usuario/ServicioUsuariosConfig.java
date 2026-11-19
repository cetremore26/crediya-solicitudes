package co.com.crediya.client.usuario;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class ServicioUsuariosConfig {
    private static final Duration TIEMPO_ESPERA = Duration.ofSeconds(3);

    @Bean
    public RestClient usuariosRestClient(@Value("${usuarios.service.url}") String baseUrl) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(TIEMPO_ESPERA);
        requestFactory.setReadTimeout(TIEMPO_ESPERA);
        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .requestInterceptor(new PropagadorAutorizacionInterceptor())
                .build();
    }
}
