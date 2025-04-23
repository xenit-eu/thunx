package com.contentgrid.thunx.predicates.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class LogicalOperation implements BooleanOperation {

    private final Operator operator;
    private List<ThunkExpression<Boolean>> terms;

    protected LogicalOperation(Operator operator, Stream<ThunkExpression<Boolean>> terms) {
        this.operator = operator;
        this.terms = terms.collect(Collectors.toUnmodifiableList());
    }

    public static LogicalOperation disjunction(Stream<ThunkExpression<Boolean>> terms) {
        return new LogicalOperation(Operator.OR, terms);
    }

    public static LogicalOperation disjunction(List<ThunkExpression<Boolean>> terms) {
        return disjunction(terms.stream());
    }

    public static LogicalOperation uncheckedDisjunction(List<ThunkExpression<?>> terms) {
        return disjunction(terms.stream().map(expr -> (ThunkExpression<Boolean>) expr));
    }

    public static LogicalOperation disjunction(ThunkExpression<Boolean>... terms) {
        return disjunction(Arrays.stream(terms));
    }

    public static LogicalOperation conjunction(Stream<ThunkExpression<Boolean>> terms) {
        return new LogicalOperation(Operator.AND, terms);
    }

    public static LogicalOperation conjunction(ThunkExpression<Boolean>... terms) {
        return conjunction(Arrays.stream(terms));
    }

    public static LogicalOperation conjunction(List<ThunkExpression<Boolean>> terms) {
        return conjunction(terms.stream());
    }

    public static LogicalOperation uncheckedConjunction(List<ThunkExpression<?>> terms) {
        return conjunction(terms.stream().map(expr -> (ThunkExpression<Boolean>) expr));
    }

    public static LogicalOperation uncheckedNegation(List<ThunkExpression<?>> terms) {
        if (terms.size() != 1) {
            throw new IllegalArgumentException("Expected 1 term, not "+terms.size());
        }
        return negation((ThunkExpression<Boolean>) terms.get(0));
    }

    public static LogicalOperation negation(ThunkExpression<Boolean> term) {
        return new LogicalOperation(Operator.NOT, Stream.of(term));
    }

    @Override
    public Operator getOperator() {
        return this.operator;
    }

    @Override
    public List<ThunkExpression<?>> getTerms() {
        return Collections.unmodifiableList(this.terms);
    }

    @Override
    public String toString() {
        return this.toDebugString();
    }
}
