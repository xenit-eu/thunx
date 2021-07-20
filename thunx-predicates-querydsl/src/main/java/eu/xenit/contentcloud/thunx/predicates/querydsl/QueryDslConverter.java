package eu.xenit.contentcloud.thunx.predicates.querydsl;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Operator;
import com.querydsl.core.types.Ops;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.PathBuilder;
import eu.xenit.contentcloud.thunx.predicates.model.ThunkExpressionVisitor;
import eu.xenit.contentcloud.thunx.predicates.model.FunctionExpression;
import eu.xenit.contentcloud.thunx.predicates.model.Scalar;
import eu.xenit.contentcloud.thunx.predicates.model.SymbolicReference;
import eu.xenit.contentcloud.thunx.predicates.model.SymbolicReference.PathElementVisitor;
import eu.xenit.contentcloud.thunx.predicates.model.Variable;

class QueryDslConverter implements ThunkExpressionVisitor<Expression<?>> {

    private PathBuilder<?> subjectPathBuilder;

    QueryDslConverter(PathBuilder<?> subjectPathBuilder) {
        this.subjectPathBuilder = subjectPathBuilder;
    }



    @Override
    public Expression<?> visit(Scalar<?> scalar) {
        return Expressions.constant(scalar.getValue());
    }

    @Override
    public Expression<?> visit(FunctionExpression<?> function) {

        // convert all the terms
        var terms = function.getTerms().stream()
                .map(term -> term.accept(this))
                .toArray(Expression[]::new);



        switch (function.getOperator()) {
            // case of boolean expressions
            case EQUALS:
            case OR:
                return Expressions.predicate(toQuerydsl(function.getOperator()), terms);
            default:
                throw new UnsupportedOperationException(
                        "Operation '" + function.getOperator() + "' not implemented");
        }
    }

    @Override
    public Expression<?> visit(SymbolicReference symbolicReference) {
        // TODO check that symbolic-ref 'subject' matches the PathBuilder subject ?

        String subject = symbolicReference.getSubject().getName();
        if (!"entity".equalsIgnoreCase(subject)) {
            throw new IllegalArgumentException("Expected symbolic-ref subject named 'entity', but got '"+subject+"'");
        }

        var path = symbolicReference.getPath();
        PathBuilder<?> builder = this.subjectPathBuilder;
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
            case OR:
                return Ops.OR;
            case AND:
                return Ops.AND;
            default:
                throw new UnsupportedOperationException("operator '" + operator + "' is not implemented");
        }
    }
}
