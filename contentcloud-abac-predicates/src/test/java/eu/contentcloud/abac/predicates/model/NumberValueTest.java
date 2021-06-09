package eu.contentcloud.abac.predicates.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class NumberValueTest {

    @Test
    void equals() {
        assertThat(Scalar.of(5)).isEqualTo(Scalar.of(5));
        assertThat(Scalar.of(5)).isEqualTo(Scalar.of(5L));

        assertThat(Scalar.of(5.0)).isEqualTo(Scalar.of(5.0D));

        assertThat(Scalar.of(5)).isEqualTo(new NumberValue(BigDecimal.valueOf(5)));
        assertThat(Scalar.of(5L)).isEqualTo(new NumberValue(BigDecimal.valueOf(5)));
    }

    @Test
    void notEquals_byType() {
        assertThat(Scalar.of(5L)).isNotEqualTo(Scalar.of(5D));
        assertThat(Scalar.of(5D)).isNotEqualTo(Scalar.of(5L));
    }

    @Test
    void notEquals_byValue() {
        assertThat(Scalar.of(5L)).isNotEqualTo(Scalar.of(42L));
        assertThat(Scalar.of(5D)).isNotEqualTo(Scalar.of(42D));
    }
}