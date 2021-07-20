package eu.xenit.contentcloud.thunx.predicates.model;

public interface ThunkExpression<T> {

    Class<? extends T> getResultType();

    boolean canBeResolved();

    default T resolve() {
        throw new UnsupportedOperationException("cannot be resolved");
    }

    <R> R accept(ThunkExpressionVisitor<R> visitor);
}

