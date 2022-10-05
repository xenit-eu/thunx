package com.contentgrid.thunx.predicates.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ThunkExpressionTest {

    @Test
    void maybeValueWithDirectNull() {
        var nullExpression = Scalar.nullValue();

        assertThrows(IllegalArgumentException.class, () -> {
            ThunkExpression.maybeValue(nullExpression);
        });
    }

    @Test
    void maybeScalarWithDirectNull() {
        var nullExpression = Scalar.nullValue();

        assertThat(ThunkExpression.maybeScalar(nullExpression)).contains(Scalar.nullValue());
    }
}