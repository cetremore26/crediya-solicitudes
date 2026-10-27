package co.com.crediya.api;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    private static final String API_TITLE = "CrediYa - Solicitudes";
    private static final String API_VERSION = "v1";
    private static final String API_DESCRIPTION =
            "Microservicio de gestión de solicitudes de préstamo de CrediYa.";

    @Bean
    public OpenAPI openApi() {
        return new OpenAPI().info(new Info()
                .title(API_TITLE)
                .version(API_VERSION)
                .description(API_DESCRIPTION));
    }
}
