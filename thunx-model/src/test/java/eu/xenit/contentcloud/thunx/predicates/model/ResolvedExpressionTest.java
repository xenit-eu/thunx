package eu.xenit.contentcloud.thunx.predicates.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ResolvedExpressionTest {

    @Test
    void maybeResultWithNull() {
        var expression = Comparison.areEqual(
                SymbolicReference.parse("input.entity.deletedAt"),
                Scalar.nullValue()
        );

        var simplified = expression.simplify();

        assertThat(simplified).isEqualTo(expression);
    }

    @Test
    void maybeResultWithDirectNull() {
        var nullExpression = Scalar.nullValue().simplify();

        assertThrows(IllegalArgumentException.class, () -> {
            ResolvedExpression.maybeResult(nullExpression);
        });
    }

    @Test
    void maybeResolvedExpressionWithDirectNull() {
        var nullExpression = Scalar.nullValue().simplify();

        assertThat(ResolvedExpression.maybeResolvedExpression(nullExpression)).containsInstanceOf(ResolvedExpression.class);
    }
}