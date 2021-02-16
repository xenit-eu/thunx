package eu.xenit.contentcloud.security.pbac.predicates.model;

import static eu.xenit.contentcloud.security.pbac.predicates.model.SymbolicReference.path;
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
                        SymbolicReference.of(Variable.named("input"), path("radius"))
                ),
                Scalar.of(2)),
            Scalar.of(4));

        assertThat(expression).isNotNull();
        assertThat(expression.getResultType()).isEqualTo(Boolean.class);
    }

}
