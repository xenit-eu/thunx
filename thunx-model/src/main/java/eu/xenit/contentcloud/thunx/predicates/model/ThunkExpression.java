package eu.xenit.contentcloud.thunx.predicates.model;

import java.util.Optional;

public interface ThunkExpression<T> {

    Class<? extends T> getResultType();

    default <R> ThunkExpression<R> assertResultType(Class<R> resultType) {
        if(resultType != getResultType()) {
            throw new IllegalArgumentException("Result of expression "+this+" is "+getResultType()+", which does not match the asserted type "+resultType+".");
        }
        return (ThunkExpression<R>) this;
    }

    <R> R accept(ThunkExpressionVisitor<R> visitor);

    static <R> Optional<Scalar<R>> maybeScalar(ThunkExpression<R> expression) {
        if(expression instanceof Scalar) {
            return Optional.of((Scalar<R>) expression);
        }
        return Optional.empty();
    }

    static <R> Optional<R> maybeValue(ThunkExpression<R> expression) {
        if(expression.getResultType() == Void.class) {
            throw new IllegalArgumentException("Expression with result type "+Void.class+" can not be mapped with #maybeValue(), use #maybeScalar() instead.");
        }
        return maybeScalar(expression)
                .map(Scalar::getValue);
    }

}

