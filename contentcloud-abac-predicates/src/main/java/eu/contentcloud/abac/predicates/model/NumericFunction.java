package eu.contentcloud.abac.predicates.model;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
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
    private final Expression<?> leftTerm;

    @Getter
    @NonNull
    private final Expression<?> rightTerm;

    @Override
    public List<Expression<?>> getTerms() {
        return List.of(this.leftTerm, this.rightTerm);
    }

    @Override
    public Class<? extends Number> getResultType() {
        return Number.class;
    }

    @Override
    public boolean canBeResolved() {
        return this.leftTerm.canBeResolved() && this.rightTerm.canBeResolved();
    }

    public static NumericFunction multiply(Expression<?> left, Expression<?> right) {
        return new NumericFunction(Operator.MULTIPLY, left, right);
    }

    public static NumericFunction multiply(List<Expression<?>> terms) {
        // this _could_ be actually 1..N number of terms
        // requiring N=2 for now
        Objects.requireNonNull(terms, "terms cannot be null");
        if (terms.size() != 2) {
            throw new IllegalArgumentException("Expected 2 terms, but got "+terms.size());
        }

        return NumericFunction.multiply(terms.get(0), terms.get(1));
    }

    public static NumericFunction plus(Expression<?> left, Expression<?> right) {
        return new NumericFunction(Operator.PLUS, left, right);
    }

}
