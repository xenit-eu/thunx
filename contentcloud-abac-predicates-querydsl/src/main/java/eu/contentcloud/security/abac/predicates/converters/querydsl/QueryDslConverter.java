package eu.contentcloud.security.abac.predicates.converters.querydsl;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Operator;
import com.querydsl.core.types.Ops;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.PathBuilder;
import eu.contentcloud.abac.predicates.model.ExpressionVisitor;
import eu.contentcloud.abac.predicates.model.FunctionExpression;
import eu.contentcloud.abac.predicates.model.Scalar;
import eu.contentcloud.abac.predicates.model.SymbolicReference;
import eu.contentcloud.abac.predicates.model.SymbolicReference.PathElementVisitor;
import eu.contentcloud.abac.predicates.model.Variable;

class QueryDslConverter implements ExpressionVisitor<Expression<?>> {

    private PathBuilder subjectPathBuilder;

    QueryDslConverter(PathBuilder subjectPathBuilder) {

        this.subjectPathBuilder = subjectPathBuilder;
    }



    @Override
    public Expression<?> visit(Scalar<?> scalar) {
        return Expressions.constant(scalar.getValue());
    }

    @Override
    public Expression<?> visit(FunctionExpression<?> function) {
        switch (function.getOperator()) {
            case EQUALS:
                var terms = function.getTerms().stream()
                        .map(term -> term.accept(this))
                        .toArray(Expression[]::new);

                return Expressions.predicate(
                        toQuerydsl(function.getOperator()),
                        terms
                );
            default:
                throw new UnsupportedOperationException(
                        "Operation '" + function.getOperator() + "' not implemented");
        }
    }

    @Override
    public Expression<?> visit(SymbolicReference symbolicReference) {
        // TODO check that symbolic-ref 'subject' matches the PathBuilder subject ?
        var path = symbolicReference.getPath();

        PathBuilder<Object> builder = this.subjectPathBuilder;
        for (var elem : path) {
            var result = elem.accept(new PathElementVisitor<String>() {
                @Override
                public String visit(Scalar<?> scalar) {
                    if (scalar.getResultType().equals(String.class)) {
                        return (String) scalar.getValue();
                    } else {
                        throw new UnsupportedOperationException(
                                "cannot traverse symbolic reference using scalar of type " + scalar.getResultType()
                                        .getSimpleName());
                    }
                }

                @Override
                public String visit(Variable variable) {
                    return variable.getName();
                }
            });

            builder = builder.get(result);
        }

        return builder;
    }

    @Override
    public Expression<?> visit(Variable variable) {
        // TODO could there be more variables available, than just the subject-path-builder ?
        throw new UnsupportedOperationException("converting variable to querydsl is not yet implemented");
    }

    public static Operator toQuerydsl(FunctionExpression.Operator operator) {
        switch (operator) {
            case EQUALS:
                return Ops.EQ;
            default:
                throw new UnsupportedOperationException("operator '" + operator + "' is not implemented");
        }
    }
}
