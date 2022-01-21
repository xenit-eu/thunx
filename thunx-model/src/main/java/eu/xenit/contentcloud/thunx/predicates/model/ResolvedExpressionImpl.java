package eu.xenit.contentcloud.thunx.predicates.model;

import lombok.AllArgsConstructor;

@AllArgsConstructor
class ResolvedExpressionImpl<T> implements ResolvedExpression<T> {

    private final Scalar<T> result;

    @Override
    public ThunkExpression<T> simplify() {
        return this;
    }

    @Override
    public <R> R accept(ThunkExpressionVisitor<R> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        return "RESOLVED("+ result +")";
    }

    @Override
    public Class<? extends T> getResultType() {
        return result.getResultType();
    }

    @Override
    public T getResult() {
        return result.getValue();
    }
}
