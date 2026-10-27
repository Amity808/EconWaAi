package org.ecom.econwaai.common.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KoboTest {

    @Test
    @DisplayName("Factory methods construct correct kobo amounts")
    void testFactoryMethods() {
        assertThat(Kobo.of(5000L).amount()).isEqualTo(5000L);
        assertThat(Kobo.ofNaira(50L).amount()).isEqualTo(5000L);
        assertThat(Kobo.ofNaira(50.50).amount()).isEqualTo(5050L);
        assertThat(Kobo.ofNaira(new BigDecimal("1250.75")).amount()).isEqualTo(125075L);
        assertThat(Kobo.ZERO.amount()).isEqualTo(0L);
    }

    @Test
    @DisplayName("Arithmetic additions and subtractions preserve exact amounts")
    void testArithmetic() {
        Kobo a = Kobo.ofNaira(1500L); // 150,000 kobo
        Kobo b = Kobo.ofNaira(500L);  // 50,000 kobo

        assertThat(a.plus(b)).isEqualTo(Kobo.ofNaira(2000L));
        assertThat(a.minus(b)).isEqualTo(Kobo.ofNaira(1000L));

        Kobo multiplied = b.multiply(3L);
        assertThat(multiplied).isEqualTo(Kobo.ofNaira(1500L));

        Kobo divided = a.divide(3L);
        assertThat(divided).isEqualTo(Kobo.ofNaira(500L));
    }

    @Test
    @DisplayName("Division by zero throws ArithmeticException")
    void testDivisionByZero() {
        Kobo a = Kobo.ofNaira(100L);
        assertThatThrownBy(() -> a.divide(0L))
                .isInstanceOf(ArithmeticException.class)
                .hasMessageContaining("Division by zero");
    }

    @Test
    @DisplayName("Percentage and basis points calculations work accurately")
    void testPercentageCalculations() {
        Kobo principal = Kobo.ofNaira(1_000_000L);

        // VAT @ 7.5% (750 basis points) = ₦75,000
        Kobo vatFromBasisPoints = principal.percentageBasisPoints(750);
        assertThat(vatFromBasisPoints).isEqualTo(Kobo.ofNaira(75_000L));

        Kobo vatFromDouble = principal.percentage(7.5);
        assertThat(vatFromDouble).isEqualTo(Kobo.ofNaira(75_000L));
    }

    @Test
    @DisplayName("Currency formatting renders standard Nigerian Naira symbol and commas")
    void testFormatting() {
        assertThat(Kobo.ofNaira(1_500_000L).formatNaira()).isEqualTo("₦1,500,000.00");
        assertThat(Kobo.of(2550L).formatNaira()).isEqualTo("₦25.50");
        assertThat(Kobo.of(99L).formatNaira()).isEqualTo("₦0.99");
        assertThat(Kobo.ZERO.formatNaira()).isEqualTo("₦0.00");
        assertThat(Kobo.of(-5000L).formatNaira()).isEqualTo("-₦50.00");
    }

    @Test
    @DisplayName("Comparisons and predicate checks behave properly")
    void testComparisons() {
        Kobo low = Kobo.ofNaira(100L);
        Kobo high = Kobo.ofNaira(200L);

        assertThat(low.isLessThan(high)).isTrue();
        assertThat(high.isGreaterThan(low)).isTrue();
        assertThat(low.isPositive()).isTrue();
        assertThat(Kobo.ZERO.isZero()).isTrue();
        assertThat(Kobo.of(-10L).isNegative()).isTrue();
        assertThat(low.compareTo(high)).isNegative();
    }
}
