package eu.xenit.contentcloud.thunx.predicates.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
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

    @Test
    void mixedSimplifyableExpressionsExample() {
        // 5 == 5 AND (FALSE OR (input.number == 1 AND TRUE) OR input.number == 3) AND input.str == "x" AND ("abc" == "abc")
        var expression = LogicalOperation.conjunction(
                Comparison.areEqual(Scalar.of(5), Scalar.of(5)),
                LogicalOperation.disjunction(
                        Scalar.of(false),
                        LogicalOperation.disjunction(Collections.emptyList()),
                        LogicalOperation.conjunction(
                                Comparison.areEqual(
                                        SymbolicReference.parse("input.number"),
                                        Scalar.of(1)
                                ),
                                Scalar.of(true)
                        ),
                        Comparison.areEqual(
                                SymbolicReference.parse("input.number"),
                                Scalar.of(3)
                        )
                ),
                Comparison.areEqual(
                        SymbolicReference.parse("input.str"),
                        Scalar.of("x")
                ),
                Comparison.areEqual(
                        Scalar.of("abc"),
                        Scalar.of("abc")
                ),
                LogicalOperation.conjunction(Collections.emptyList())
        );

        // This simplifies to: (input.number == 1 OR input.number == 3) AND input.str == "x"
        var simplified = LogicalOperation.conjunction(
                LogicalOperation.disjunction(
                        Comparison.areEqual(
                                SymbolicReference.parse("input.number"),
                                Scalar.of(1)
                        ),
                        Comparison.areEqual(
                                SymbolicReference.parse("input.number"),
                                Scalar.of(3)
                        )
                ),
                Comparison.areEqual(
                        SymbolicReference.parse("input.str"),
                        Scalar.of("x")

                )
        );

        assertThat(expression.simplify()).isEqualTo(simplified);

        assertThat(simplified.simplify()).isEqualTo(simplified);
    }

}
