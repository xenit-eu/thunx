package eu.xenit.contentcloud.thunx.predicates.model;

public interface ThunkExpression<T> {

    Class<? extends T> getResultType();

    ThunkExpression<T> simplify();

    <R> R accept(ThunkExpressionVisitor<R> visitor);
}

