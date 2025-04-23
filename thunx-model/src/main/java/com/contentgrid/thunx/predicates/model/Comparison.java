package com.contentgrid.thunx.predicates.model;

import java.util.List;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
public class Comparison implements BooleanOperation {

    @Getter
    @NonNull
    private final Operator operator;

    @Getter
    @NonNull
    private final ThunkExpression<?> leftTerm;

    @Getter
    @NonNull
    private final ThunkExpression<?> rightTerm;


    @Override
    public List<ThunkExpression<?>> getTerms() {
        return List.of(this.leftTerm, this.rightTerm);
    }

    public static Comparison areEqual(List<ThunkExpression<?>> terms) {
        assertTermSizeIsTwo(terms);
        return areEqual(terms.get(0), terms.get(1));
    }

    public static Comparison areEqual(ThunkExpression<?> left, ThunkExpression<?> right) {
        return new Comparison(Operator.EQUALS, left, right);
    }

    public static Comparison notEqual(List<ThunkExpression<?>> terms) {
        assertTermSizeIsTwo(terms);
        return notEqual(terms.get(0), terms.get(1));
    }

    public static Comparison notEqual(ThunkExpression<?> left, ThunkExpression<?> right) {
        return new Comparison(Operator.NOT_EQUAL_TO, left, right);
    }

    public static Comparison greater(List<ThunkExpression<?>> terms) {
        assertTermSizeIsTwo(terms);
        return greater(terms.get(0), terms.get(1));
    }

    public static Comparison greater(ThunkExpression<?> left, ThunkExpression<?> right) {
        return new Comparison(Operator.GREATER_THAN, left, right);
    }

    public static Comparison greaterOrEquals(List<ThunkExpression<?>> terms) {
        assertTermSizeIsTwo(terms);
        return greaterOrEquals(terms.get(0), terms.get(1));
    }

    public static Comparison greaterOrEquals(ThunkExpression<?> left, ThunkExpression<?> right) {
        return new Comparison(Operator.GREATER_THAN_OR_EQUAL_TO, left, right);
    }

    public static Comparison less(@NonNull List<ThunkExpression<?>> terms) {
        assertTermSizeIsTwo(terms);
        return less(terms.get(0), terms.get(1));
    }

    public static Comparison less(ThunkExpression<?> left, ThunkExpression<?> right) {
        return new Comparison(Operator.LESS_THAN, left, right);
    }

    public static Comparison lessOrEquals(@NonNull List<ThunkExpression<?>> terms) {
        assertTermSizeIsTwo(terms);
        return lessOrEquals(terms.get(0), terms.get(1));
    }

    public static Comparison lessOrEquals(ThunkExpression<?> left, ThunkExpression<?> right) {
        return new Comparison(Operator.LESS_THEN_OR_EQUAL_TO, left, right);
    }

    private static void assertTermSizeIsTwo(List<ThunkExpression<?>> terms) {
        if (terms.size() == 2) {
            return;
        }
        throw new IllegalArgumentException("Expected 2 terms, but got " + terms.size());
    }

    @Override
    public String toString() {
        return this.toDebugString();
    }

}
