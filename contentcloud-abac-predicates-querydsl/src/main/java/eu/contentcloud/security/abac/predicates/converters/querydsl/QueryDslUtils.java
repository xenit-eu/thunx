package eu.contentcloud.security.abac.predicates.converters.querydsl;

import com.querydsl.core.types.Operator;
import com.querydsl.core.types.Ops;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.PathBuilder;
import eu.contentcloud.abac.predicates.model.Expression;
import eu.contentcloud.abac.predicates.model.ExpressionVisitor;
import eu.contentcloud.abac.predicates.model.FunctionExpression;
import eu.contentcloud.abac.predicates.model.Scalar;
import eu.contentcloud.abac.predicates.model.SymbolicReference;
import eu.contentcloud.abac.predicates.model.SymbolicReference.PathElementVisitor;
import eu.contentcloud.abac.predicates.model.Variable;

public class QueryDslUtils {

    public static Predicate from(Expression<Boolean> abacExpr, PathBuilder entityPath) {

        var queryDslExpr = abacExpr.accept(new QueryDslConverter(entityPath));
        return (Predicate) queryDslExpr;
    }

    static class QueryDslConverter implements ExpressionVisitor<com.querydsl.core.types.Expression<?>> {

        private PathBuilder subjectPathBuilder;

        public QueryDslConverter(PathBuilder subjectPathBuilder) {

            this.subjectPathBuilder = subjectPathBuilder;
        }

        @Override
        public com.querydsl.core.types.Expression<?> visit(Scalar<?> scalar) {
            return Expressions.constant(scalar.getValue());
        }

        @Override
        public com.querydsl.core.types.Expression<?> visit(FunctionExpression<?> function) {
            switch (function.getOperator()) {
                case EQUALS:
                    var terms = function.getTerms().stream()
                            .map(term -> term.accept(this))
                            .toArray(com.querydsl.core.types.Expression[]::new);

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
        public com.querydsl.core.types.Expression<?> visit(SymbolicReference symbolicReference) {
            // TODO check that symbolic-ref 'subject' mathces the PathBuilder subject ?
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
        public com.querydsl.core.types.Expression<?> visit(Variable variable) {

//            variable.ge
            return null;
        }
    }

    public static Operator toQuerydsl(eu.contentcloud.abac.predicates.model.FunctionExpression.Operator operator) {
        switch (operator) {
            case EQUALS:
                return Ops.EQ;
            default:
                throw new UnsupportedOperationException("operator '" + operator + "' is not implemented");
        }
    }
}

