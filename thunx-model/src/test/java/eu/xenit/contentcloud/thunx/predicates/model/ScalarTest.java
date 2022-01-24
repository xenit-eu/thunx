package eu.xenit.contentcloud.thunx.predicates.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ScalarTest {

    @Test
    void maybeValueWithNull() {
        var expression = Comparison.areEqual(
                SymbolicReference.parse("input.entity.deletedAt"),
                Scalar.nullValue()
        );

        var simplified = expression.simplify();

        assertThat(simplified).isEqualTo(expression);
    }

    @Test
    void maybeValueWithDirectNull() {
        var nullExpression = Scalar.nullValue().simplify();

        assertThrows(IllegalArgumentException.class, () -> {
            Scalar.maybeValue(nullExpression);
        });
    }

    @Test
    void maybeScalarWithDirectNull() {
        var nullExpression = Scalar.nullValue().simplify();

        assertThat(Scalar.maybeScalar(nullExpression)).contains(Scalar.nullValue());
    }
}