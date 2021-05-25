package eu.xenit.contentcloud.security.pbac.predicates.model;

import static org.assertj.core.api.Assertions.assertThat;

import eu.xenit.contentcloud.security.pbac.predicates.model.FunctionExpression.Operator;
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

}