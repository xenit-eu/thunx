package com.contentgrid.thunx.predicates.model;

public interface ThunkExpressionVisitor<T, C> {

    T visit(Scalar<?> scalar, C context);
    T visit(FunctionExpression<?> functionExpression, C context);
    T visit(SymbolicReference symbolicReference, C context);
    T visit(Variable variable, C context);
    T visit(SetValue setValue, C context);
    T visit(ListValue listValue, C context);

}
