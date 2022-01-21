package eu.xenit.contentcloud.thunx.predicates.model;

import java.util.Optional;

public interface ResolvedExpression<T> extends ThunkExpression<T> {
    T getResult();

    static <R> Optional<R> maybeResult(ThunkExpression<R> expression) {
        if(expression instanceof ResolvedExpression) {
            return Optional.of(((ResolvedExpression<R>) expression).getResult());
        }
        return Optional.empty();
    }
}
