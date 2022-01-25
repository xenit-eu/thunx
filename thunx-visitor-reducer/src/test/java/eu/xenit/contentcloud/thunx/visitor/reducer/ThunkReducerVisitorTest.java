package eu.xenit.contentcloud.thunx.visitor.reducer;

import static org.assertj.core.api.Assertions.assertThat;

import eu.xenit.contentcloud.thunx.predicates.model.Comparison;
import eu.xenit.contentcloud.thunx.predicates.model.LogicalOperation;
import eu.xenit.contentcloud.thunx.predicates.model.Scalar;
import eu.xenit.contentcloud.thunx.predicates.model.SymbolicReference;
import eu.xenit.contentcloud.thunx.predicates.model.ThunkExpression;
import java.util.Collections;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ThunkReducerVisitorTest {
    @Nested
    class CompareEquals {

        @Test
        void compareEquals_simplify_non_equal() {
            var comparison = Comparison.areEqual(Scalar.of(5), Scalar.of(3));
            assertThat(ThunkExpression.maybeValue(ThunkReducerVisitor.DEFAULT_INSTANCE.visit(comparison).assertResultType(Boolean.class))).contains(false);
        }

        @Test
        void compareEquals_simplify_non_equal_types() {
            var comparison = Comparison.areEqual(Scalar.of(5), Scalar.of("5"));
            assertThat(ThunkExpression.maybeValue(ThunkReducerVisitor.DEFAULT_INSTANCE.visit(comparison).assertResultType(Boolean.class))).contains(false);
        }

        @Test
        void compareEquals_simplify_equal() {
            var comparison = Comparison.areEqual(Scalar.of(5), Scalar.of(5));
            assertThat(ThunkExpression.maybeValue(ThunkReducerVisitor.DEFAULT_INSTANCE.visit(comparison).assertResultType(Boolean.class))).contains(true);
        }
    }

    @Nested
    class Logical {

        @Test
        void resolvableDisjunction() {
            var disjunction = LogicalOperation.disjunction(Scalar.of(true));

            var simplified = ThunkReducerVisitor.DEFAULT_INSTANCE.visit(disjunction);
            var resolved = ThunkExpression.maybeValue(simplified.assertResultType(Boolean.class));
            assertThat(resolved).isPresent().contains(true);
        }

        @Test
        void resolvableDisjunctions_someResolvableTrue() {
            var disjunction = LogicalOperation.disjunction(
                    Scalar.of(true),
                    Comparison.areEqual(SymbolicReference.of("document.security"), Scalar.of(5))
            );

            var simplified = ThunkReducerVisitor.DEFAULT_INSTANCE.visit(disjunction);
            var resolved = ThunkExpression.maybeValue(simplified.assertResultType(Boolean.class));
            assertThat(resolved).isPresent().contains(true);
        }

        @Test
        void resolvableDisjunctions_someResolvableFalse() {
            var disjunction = LogicalOperation.disjunction(
                    Scalar.of(false),
                    Comparison.areEqual(SymbolicReference.of("document.security"), Scalar.of(5))
            );

            var simplified = ThunkReducerVisitor.DEFAULT_INSTANCE.visit(disjunction);
            assertThat(simplified).hasToString("EQ(document.security, 5)");

            var resolved = ThunkExpression.maybeValue(simplified);
            assertThat(resolved).isNotPresent();
        }

        @Test
        void resolvableDisjunctions_empty() {
            var disjunction = LogicalOperation.disjunction(Collections.emptyList());

            var simplified = ThunkReducerVisitor.DEFAULT_INSTANCE.visit(disjunction);
            var resolved = ThunkExpression.maybeValue(simplified.assertResultType(Boolean.class));
            assertThat(resolved).isPresent().contains(false);
        }

        @Test
        void resolvableConjunction() {
            var conjunction = LogicalOperation.conjunction(Scalar.of(true));

            var simplified = ThunkReducerVisitor.DEFAULT_INSTANCE.visit(conjunction);
            var resolved = ThunkExpression.maybeValue(simplified.assertResultType(Boolean.class));
            assertThat(resolved).isPresent().contains(true);
        }

        @Test
        void resolvableConjunctions_someResolvableFalse() {
            var conjunction = LogicalOperation.conjunction(
                    Scalar.of(false),
                    Comparison.areEqual(SymbolicReference.of("document.security"), Scalar.of(5))
            );

            var simplified = ThunkReducerVisitor.DEFAULT_INSTANCE.visit(conjunction);
            var resolved = ThunkExpression.maybeValue(simplified.assertResultType(Boolean.class));
            assertThat(resolved).isPresent().contains(false);
        }

        @Test
        void resolvableConjunctions_someResolvableTrue() {
            var conjunction = LogicalOperation.conjunction(
                    Scalar.of(true),
                    Comparison.areEqual(SymbolicReference.of("document.security"), Scalar.of(5))
            );

            var simplified = ThunkReducerVisitor.DEFAULT_INSTANCE.visit(conjunction);
            assertThat(simplified).hasToString("EQ(document.security, 5)");

            var resolved = ThunkExpression.maybeValue(simplified);
            assertThat(resolved).isNotPresent();
        }

        @Test
        void resolvableConjunctions_empty() {
            var conjunction = LogicalOperation.conjunction(Collections.emptyList());

            var simplified = ThunkReducerVisitor.DEFAULT_INSTANCE.visit(conjunction);
            var resolved = ThunkExpression.maybeValue(simplified.assertResultType(Boolean.class));
            assertThat(resolved).isPresent().contains(true);
        }
    }

    @Test
    void mixedReduceableExpressionsExample() {
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

        assertThat(ThunkReducerVisitor.DEFAULT_INSTANCE.visit(expression)).isEqualTo(simplified);

        assertThat(ThunkReducerVisitor.DEFAULT_INSTANCE.visit(simplified)).isEqualTo(simplified);
    }
}