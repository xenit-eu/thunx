package eu.contentcloud.abac.predicates.model;

public interface ExpressionVisitor<T> {

    T visit(Scalar<?> scalar);
    T visit(FunctionExpression<?> functionExpression);
    T visit(SymbolicReference symbolicReference);
    T visit(Variable variable);

}
