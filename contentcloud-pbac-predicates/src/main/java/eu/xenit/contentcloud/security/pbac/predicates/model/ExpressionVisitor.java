package eu.xenit.contentcloud.security.pbac.predicates.model;

public interface ExpressionVisitor<T> {

    T visit(Scalar<?> scalar);
    T visit(FunctionExpression<?> functionExpression);
    T visit(SymbolicReference symbolicReference);
    T visit(Variable variable);

}
