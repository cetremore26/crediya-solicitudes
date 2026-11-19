package co.com.crediya.model.solicitud;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/** Cuota mensual de un préstamo con el sistema de amortización francés. */
public final class CalculadoraCuotaMensual {
    private static final int MESES_POR_ANIO = 12;
    private static final BigDecimal CIEN = BigDecimal.valueOf(100);
    private static final int ESCALA_MONEDA = 2;

    private CalculadoraCuotaMensual() {
    }

    /**
     * @param monto capital del préstamo
     * @param plazoMeses número de cuotas mensuales
     * @param tasaAnualPorcentaje tasa de interés nominal anual, en porcentaje (p. ej. 18 para 18%)
     */
    public static BigDecimal calcular(
            BigDecimal monto, int plazoMeses, BigDecimal tasaAnualPorcentaje) {
        BigDecimal tasaMensual = tasaAnualPorcentaje
                .divide(CIEN, MathContext.DECIMAL64)
                .divide(BigDecimal.valueOf(MESES_POR_ANIO), MathContext.DECIMAL64);
        if (tasaMensual.signum() == 0) {
            return monto.divide(BigDecimal.valueOf(plazoMeses), ESCALA_MONEDA, RoundingMode.HALF_UP);
        }
        BigDecimal crecimiento = BigDecimal.ONE.add(tasaMensual).pow(plazoMeses, MathContext.DECIMAL64);
        return monto.multiply(tasaMensual).multiply(crecimiento)
                .divide(crecimiento.subtract(BigDecimal.ONE), MathContext.DECIMAL64)
                .setScale(ESCALA_MONEDA, RoundingMode.HALF_UP);
    }
}
