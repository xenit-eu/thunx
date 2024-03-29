package com.contentgrid.thunx.predicates.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.contentgrid.thunx.predicates.model.FunctionExpression.Operator;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class LogicalOperationTest {

    @Test
    void disjunction() {
        // rules: document.security <= 5 OR user.admin == true OR planets_aligned == true
        var rule1 = Comparison.lessOrEquals(
                SymbolicReference.of("document", path -> path.string("security")),
                Scalar.of(5));
        var rule2 = Comparison.areEqual(SymbolicReference.of("user.admin"), Scalar.of(true));
        var rule3 = Comparison.areEqual(SymbolicReference.of("planets_aligned"), Scalar.of(3));
        var disjunction = LogicalOperation.disjunction(rule1, rule2, rule3);

        assertThat(disjunction.getOperator()).isEqualTo(Operator.OR);
        Assertions.assertThat(disjunction.getTerms()).containsExactly(rule1, rule2, rule3);
        assertThat(disjunction).isEqualTo(LogicalOperation.disjunction(rule1, rule2, rule3));
    }

    @Test
    void conjunction() {
        // rules: document.security <= 5 AND user.clothing.coat.color == "blue"
        var rule1 = Comparison.lessOrEquals(SymbolicReference.of("document.security"), Scalar.of(5));
        var rule2 = Comparison.areEqual(SymbolicReference.parse("user.clothing.coat.color"), Scalar.of("blue"));
        var conjunction = LogicalOperation.conjunction(rule1, rule2);

        assertThat(conjunction.getOperator()).isEqualTo(Operator.AND);
        Assertions.assertThat(conjunction.getTerms()).hasSize(2);

        assertThat(conjunction).isEqualTo(LogicalOperation.conjunction(rule1, rule2));
    }


    @Test
    void conjunction_toString() {
        // rules: document.security <= 5 AND user.clothing.coat.color == "blue"
        var rule1 = Comparison.lessOrEquals(SymbolicReference.of("document.security"), Scalar.of(5));
        var rule2 = Comparison.areEqual(SymbolicReference.parse("user.clothing.coat.color"), Scalar.of("blue"));
        var conjunction = LogicalOperation.conjunction(rule1, rule2);

        assertThat(conjunction).hasToString("AND(LTE(document.security, 5), EQ(user.clothing.coat.color, 'blue'))");

    }

}