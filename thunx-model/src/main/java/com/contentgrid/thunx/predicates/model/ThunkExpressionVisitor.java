package com.contentgrid.thunx.predicates.model;

public interface ThunkExpressionVisitor<T, C> {

    T visit(Scalar<?> scalar, C context);
    T visit(FunctionExpression<?> functionExpression, C context);
    T visit(SymbolicReference symbolicReference, C context);
    T visit(Variable variable, C context);
    default T visit(CollectionValue collectionValue, C context) {
        //TODO remove this and implement this method in all visitors
        throw new UnsupportedOperationException("Visit for CollectionValue is not yet implemented.");
    };

}
