package org.ecom.econwaai.common.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Objects;

/**
 * Immutable financial primitive representing monetary values in Nigerian Kobo (1 Naira = 100 Kobo).
 * Avoids floating-point rounding errors and enforces exact integer arithmetic.
 */
public record Kobo(long amount) implements Comparable<Kobo>, Serializable {

    public static final Kobo ZERO = new Kobo(0L);

    public static Kobo of(long kobo) {
        return new Kobo(kobo);
    }

    public static Kobo ofNaira(long naira) {
        return new Kobo(Math.multiplyExact(naira, 100L));
    }

    public static Kobo ofNaira(double naira) {
        return new Kobo(Math.round(naira * 100.0));
    }

    public static Kobo ofNaira(BigDecimal naira) {
        Objects.requireNonNull(naira, "Naira amount cannot be null");
        return new Kobo(naira.multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP).longValueExact());
    }

    public Kobo plus(Kobo other) {
        Objects.requireNonNull(other, "Operand Kobo cannot be null");
        return new Kobo(Math.addExact(this.amount, other.amount));
    }

    public Kobo minus(Kobo other) {
        Objects.requireNonNull(other, "Operand Kobo cannot be null");
        return new Kobo(Math.subtractExact(this.amount, other.amount));
    }

    public Kobo multiply(long factor) {
        return new Kobo(Math.multiplyExact(this.amount, factor));
    }

    public Kobo multiply(double factor) {
        return new Kobo(Math.round(this.amount * factor));
    }

    public Kobo divide(long divisor) {
        if (divisor == 0) {
            throw new ArithmeticException("Division by zero");
        }
        return new Kobo(Math.round((double) this.amount / divisor));
    }

    /**
     * Calculates percentage using basis points (1 basis point = 0.01% = 0.0001).
     * Example: 750 basis points = 7.50% VAT.
     */
    public Kobo percentageBasisPoints(int basisPoints) {
        long calculated = Math.round((double) this.amount * basisPoints / 10000.0);
        return new Kobo(calculated);
    }

    public Kobo percentage(double percent) {
        long calculated = Math.round((double) this.amount * (percent / 100.0));
        return new Kobo(calculated);
    }

    public boolean isPositive() {
        return this.amount > 0L;
    }

    public boolean isZero() {
        return this.amount == 0L;
    }

    public boolean isNegative() {
        return this.amount < 0L;
    }

    public boolean isGreaterThan(Kobo other) {
        return this.compareTo(other) > 0;
    }

    public boolean isLessThan(Kobo other) {
        return this.compareTo(other) < 0;
    }

    public BigDecimal toNairaDecimal() {
        return BigDecimal.valueOf(this.amount, 2);
    }

    /**
     * Formats the monetary value into a human-readable Nigerian Naira string with ₦ symbol.
     * Example: ₦1,250,500.00 or -₦450.00
     */
    public String formatNaira() {
        BigDecimal naira = toNairaDecimal();
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        symbols.setGroupingSeparator(',');
        symbols.setDecimalSeparator('.');

        DecimalFormat df = new DecimalFormat("#,##0.00", symbols);
        if (naira.compareTo(BigDecimal.ZERO) < 0) {
            return "-₦" + df.format(naira.abs());
        }
        return "₦" + df.format(naira);
    }

    @Override
    public int compareTo(Kobo o) {
        Objects.requireNonNull(o, "Cannot compare to null Kobo");
        return Long.compare(this.amount, o.amount);
    }

    @Override
    public String toString() {
        return formatNaira();
    }
}
