package co.com.crediya.usecase.solicitud;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/** Monthly payment of a loan with the French amortization system. */
public final class CuotaMensualCalculator {
    private static final int MONTHS_PER_YEAR = 12;
    private static final BigDecimal PERCENT = BigDecimal.valueOf(100);
    private static final int CURRENCY_SCALE = 2;

    private CuotaMensualCalculator() {
    }

    /**
     * @param monto loan principal
     * @param plazoMeses number of monthly payments
     * @param tasaAnualPorcentaje nominal annual interest rate, in percent (e.g. 18 for 18%)
     */
    public static BigDecimal calculate(
            BigDecimal monto, int plazoMeses, BigDecimal tasaAnualPorcentaje) {
        BigDecimal monthlyRate = tasaAnualPorcentaje
                .divide(PERCENT, MathContext.DECIMAL64)
                .divide(BigDecimal.valueOf(MONTHS_PER_YEAR), MathContext.DECIMAL64);
        if (monthlyRate.signum() == 0) {
            return monto.divide(BigDecimal.valueOf(plazoMeses), CURRENCY_SCALE, RoundingMode.HALF_UP);
        }
        BigDecimal growth = BigDecimal.ONE.add(monthlyRate).pow(plazoMeses, MathContext.DECIMAL64);
        return monto.multiply(monthlyRate).multiply(growth)
                .divide(growth.subtract(BigDecimal.ONE), MathContext.DECIMAL64)
                .setScale(CURRENCY_SCALE, RoundingMode.HALF_UP);
    }
}
