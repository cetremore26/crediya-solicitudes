package co.com.crediya.model.solicitud;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class CuotaMensualCalculatorTest {

    @Test
    void dividesPrincipalEvenlyWhenRateIsZero() {
        assertEquals(new BigDecimal("100000.00"),
                CuotaMensualCalculator.calculate(new BigDecimal("1200000"), 12, BigDecimal.ZERO));
    }

    @Test
    void appliesFrenchAmortizationFormula() {
        // 10.000.000 a 24 meses al 18% nominal anual (1,5% mensual) -> cuota de 499.241,02
        assertEquals(new BigDecimal("499241.02"),
                CuotaMensualCalculator.calculate(
                        new BigDecimal("10000000"), 24, new BigDecimal("18")));
    }
}
