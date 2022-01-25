package eu.xenit.contentcloud.thunx.predicates.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class Scenarios {

    @Test
    void regoPythonExample() {

        // (( 3.14 * input.radius) * 2) >= 4
        var expression = Comparison.greaterOrEquals(
            NumericFunction.multiply(
                NumericFunction.multiply(
                        Scalar.of(3.14),
                        SymbolicReference.of(Variable.named("input"), SymbolicReference.path("radius"))
                ),
                Scalar.of(2)),
            Scalar.of(4));

        assertThat(expression).isNotNull();
        assertThat(expression.getResultType()).isEqualTo(Boolean.class);
    }

}
