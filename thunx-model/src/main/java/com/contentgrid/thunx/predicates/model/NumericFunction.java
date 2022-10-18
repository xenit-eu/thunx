package com.contentgrid.thunx.predicates.model;

import java.util.List;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class NumericFunction implements FunctionExpression<Number> {

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

    @Override
    public Class<? extends Number> getResultType() {
        return Number.class;
    }

    public static NumericFunction multiply(ThunkExpression<?> left, ThunkExpression<?> right) {
        return new NumericFunction(Operator.MULTIPLY, left, right);
    }

    public static NumericFunction multiply(List<ThunkExpression<?>> terms) {
        if (terms.size() < 2) {
            throw new IllegalArgumentException("Expected 2 or more terms");
        }

        return terms.stream()
                // multiplication is an associative operation
                .reduce(NumericFunction::multiply)
                .map(NumericFunction.class::cast)
                .orElseThrow(() -> new IllegalArgumentException("Expected 1 or more terms"));
    }

    public static NumericFunction plus(ThunkExpression<?> left, ThunkExpression<?> right) {
        return new NumericFunction(Operator.PLUS, left, right);
    }


    public static NumericFunction plus(@NonNull List<ThunkExpression<?>> terms) {
        if (terms.size() < 2) {
            throw new IllegalArgumentException("Expected 2 or more terms");
        }
        return terms.stream()
                // addition is an associative operation
                .reduce(NumericFunction::plus)
                .map(NumericFunction.class::cast)
                .orElseThrow(() -> new IllegalArgumentException("Expected 1 or more terms"));
    }

    public static NumericFunction divide(@NonNull List<ThunkExpression<?>> terms) {
        if (terms.size() != 2) {
            throw new IllegalArgumentException("Expected exactly 2 terms");
        }

        return divide(terms.get(0), terms.get(1));
    }

    public static NumericFunction divide(ThunkExpression<?> left, ThunkExpression<?> right) {
        return new NumericFunction(Operator.DIVIDE, left, right);
    }

    public static NumericFunction minus(@NonNull List<ThunkExpression<?>> terms) {
        if (terms.size() != 2) {
            throw new IllegalArgumentException("Expected exactly 2 terms");
        }

        return minus(terms.get(0), terms.get(1));
    }

    public static NumericFunction minus(ThunkExpression<?> left, ThunkExpression<?> right) {
        return new NumericFunction(Operator.MINUS, left, right);
    }

    public static NumericFunction modulus(@NonNull List<ThunkExpression<?>> terms) {
        if (terms.size() != 2) {
            throw new IllegalArgumentException("Expected exactly 2 terms");
        }

        return modulus(terms.get(0), terms.get(1));
    }

    public static NumericFunction modulus(ThunkExpression<?> left, ThunkExpression<?> right) {
        return new NumericFunction(Operator.MODULUS, left, right);
    }

}
