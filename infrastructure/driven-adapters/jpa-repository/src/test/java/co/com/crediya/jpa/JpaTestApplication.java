package co.com.crediya.jpa;

import org.reactivecommons.utils.ObjectMapper;
import org.reactivecommons.utils.ObjectMapperImp;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/** Minimal Spring Boot context to test the JPA adapters against an in-memory H2 database. */
@SpringBootApplication
public class JpaTestApplication {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapperImp();
    }
}
