package eu.xenit.contentcloud.thunx.predicates.model;

public interface ThunkExpressionVisitor<T> {

    T visit(Scalar<?> scalar);
    T visit(FunctionExpression<?> functionExpression);
    T visit(SymbolicReference symbolicReference);
    T visit(Variable variable);
}
