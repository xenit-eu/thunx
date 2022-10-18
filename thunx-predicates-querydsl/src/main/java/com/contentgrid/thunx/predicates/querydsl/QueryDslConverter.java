package com.contentgrid.thunx.predicates.querydsl;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Operator;
import com.querydsl.core.types.Ops;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.PathBuilder;
import com.contentgrid.thunx.predicates.model.FunctionExpression;
import com.contentgrid.thunx.predicates.model.Scalar;
import com.contentgrid.thunx.predicates.model.SymbolicReference;
import com.contentgrid.thunx.predicates.model.SymbolicReference.PathElementVisitor;
import com.contentgrid.thunx.predicates.model.ThunkExpressionVisitor;
import com.contentgrid.thunx.predicates.model.Variable;
import java.util.List;
import java.util.stream.Collectors;

class QueryDslConverter implements ThunkExpressionVisitor<Expression<?>> {

    private PathBuilder<?> subjectPathBuilder;

    QueryDslConverter(PathBuilder<?> subjectPathBuilder) {
        this.subjectPathBuilder = subjectPathBuilder;
    }


    @Override
    public Expression<?> visit(Scalar<?> scalar) {
        if (scalar == Scalar.nullValue()) {
            return Expressions.nullExpression();
        }
        return Expressions.constant(scalar.getValue());
    }

    @Override
    public Expression<?> visit(FunctionExpression<?> function) {

        // convert all the terms
        var terms = function.getTerms().stream()
                .map(term -> term.accept(this))
                .collect(Collectors.toList());

        switch (function.getOperator()) {
            case EQUALS:
                assertTwoTerms(terms);
                return ExpressionUtils.eq((Expression<Object>) terms.get(0), terms.get(1));
            case NOT_EQUAL_TO:
                assertTwoTerms(terms);
                return ExpressionUtils.ne(terms.get(0), (Expression<Object>) terms.get(1));
            case GREATER_THAN_OR_EQUAL_TO:
                assertTwoTerms(terms);
                return Expressions.booleanOperation(Ops.GOE, terms.toArray(new Expression[0]));
            case GREATER_THAN:
                assertTwoTerms(terms);
                return Expressions.booleanOperation(Ops.GT, terms.toArray(new Expression[0]));
            case LESS_THEN_OR_EQUAL_TO:
                assertTwoTerms(terms);
                return Expressions.booleanOperation(Ops.LOE, terms.toArray(new Expression[0]));
            case LESS_THAN:
                assertTwoTerms(terms);
                return Expressions.booleanOperation(Ops.LT, terms.toArray(new Expression[0]));
            case OR:
                return ExpressionUtils.anyOf(terms.stream().map(term -> (Predicate) term).collect(Collectors.toList()));
            case AND:
                return ExpressionUtils.allOf(terms.stream().map(term -> (Predicate) term).collect(Collectors.toList()));
            case NOT:
                assertOneTerm(terms);
                return Expressions.booleanOperation(Ops.NOT, terms.get(0));
            default:
                throw new UnsupportedOperationException(
                        "Operation '" + function.getOperator() + "' not implemented");
        }
    }

    private static void assertOneTerm(List<? extends Expression<?>> terms) {
        if (terms.size() != 1) {
            throw new IllegalArgumentException("Equal operation requires 1 parameters.");
        }
    }
    private static void assertTwoTerms(List<? extends Expression<?>> terms) {
        if (terms.size() != 2) {
            throw new IllegalArgumentException("Equal operation requires 2 parameters.");
        }
    }

    @Override
    public Expression<?> visit(SymbolicReference symbolicReference) {
        // TODO check that symbolic-ref 'subject' matches the PathBuilder subject ?

        String subject = symbolicReference.getSubject().getName();
        if (!"entity".equalsIgnoreCase(subject)) {
            throw new IllegalArgumentException(
                    "Expected symbolic-ref subject named 'entity', but got '" + subject + "'");
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
