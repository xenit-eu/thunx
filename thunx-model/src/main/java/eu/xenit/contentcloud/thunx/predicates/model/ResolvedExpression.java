package eu.xenit.contentcloud.thunx.predicates.model;

import java.util.Optional;

public interface ResolvedExpression<T> extends ThunkExpression<T> {
    T getResult();

    static <R> Optional<ResolvedExpression<R>> maybeResolvedExpression(ThunkExpression<R> expression) {
        if(expression instanceof ResolvedExpression) {
            return Optional.of((ResolvedExpression<R>) expression);
        }
        return Optional.empty();
    }

    static <R> Optional<R> maybeResult(ThunkExpression<R> expression) {
        if(expression.getResultType() == Void.class) {
            throw new IllegalArgumentException("Expression with result type "+Void.class+" can not be mapped with #maybeResult(), use #maybeResolvedExpression() instead.");
        }
        return maybeResolvedExpression(expression)
                .map(ResolvedExpression::getResult);
    }

    static ResolvedExpression<Boolean> always(boolean value) {
        return new ResolvedExpressionImpl<>(Scalar.of(value));
    }
}
