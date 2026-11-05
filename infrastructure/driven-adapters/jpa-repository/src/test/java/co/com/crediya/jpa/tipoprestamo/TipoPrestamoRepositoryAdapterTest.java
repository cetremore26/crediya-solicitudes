package co.com.crediya.jpa.tipoprestamo;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.com.crediya.jpa.JpaTestApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = JpaTestApplication.class)
class TipoPrestamoRepositoryAdapterTest {

    @Autowired
    private TipoPrestamoRepositoryAdapter adapter;

    @Test
    void findsSeededLoanTypes() {
        assertTrue(adapter.existsByNombre("VEHICULO"));
        assertFalse(adapter.existsByNombre("INEXISTENTE"));
    }
}
