package com.contentgrid.thunx.predicates.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.contentgrid.thunx.predicates.model.FunctionExpression.Operator;
import org.junit.jupiter.api.Test;

class ComparisonTest {

    @Test
    void compareEquals() {
        var comparison = Comparison.areEqual(Scalar.of(5), Scalar.of(3));
        assertThat(comparison).isNotNull();
        assertThat(comparison.getOperator()).isEqualTo(Operator.EQUALS);
        assertThat(comparison.getLeftTerm()).isNotNull();
        assertThat(comparison.getRightTerm()).isNotNull();
    }

    @Test
    void greaterOrEquals() {
        var comparison = Comparison.greaterOrEquals(Scalar.of(5), Scalar.of(3));

        assertThat(comparison).isNotNull();
        assertThat(comparison.getOperator()).isEqualTo(Operator.GREATER_THAN_OR_EQUAL_TO);
        assertThat(comparison.getLeftTerm()).isNotNull();
        assertThat(comparison.getRightTerm()).isNotNull();
    }

    @Test
    void comparison_toString() {
        // rule: user.clothing.coat.color == "blue"
        var comparison = Comparison.areEqual(SymbolicReference.parse("user.clothing.coat.color"), Scalar.of("blue"));

        assertThat(comparison).hasToString("EQ(user.clothing.coat.color, 'blue')");

    }

}