package eu.contentcloud.abac.predicates.model;

public interface Expression<T> {

    Class<? extends T> getResultType();

    boolean canBeResolved();

    default T resolve() {
        throw new UnsupportedOperationException("cannot be resolved");
    }

    <R> R accept(ExpressionVisitor<R> visitor);
}

