package com.contentgrid.thunx.predicates.model;

public abstract class ContextFreeThunkExpressionVisitor<T> implements ThunkExpressionVisitor<T, Void> {

    @Override
    public T visit(Scalar<?> scalar, Void context) {
        return this.visit(scalar);
    }

    @Override
    public T visit(FunctionExpression<?> functionExpression, Void context) {
        return this.visit(functionExpression);
    }

    @Override
    public T visit(SymbolicReference symbolicReference, Void context) {
        return this.visit(symbolicReference);
    }

    @Override
    public T visit(Variable variable, Void context) {
        return this.visit(variable);
    }


    protected abstract T visit(Scalar<?> scalar);
    protected abstract T visit(FunctionExpression<?> functionExpression);
    protected abstract T visit(SymbolicReference symbolicReference);
    protected abstract T visit(Variable variable);

}
