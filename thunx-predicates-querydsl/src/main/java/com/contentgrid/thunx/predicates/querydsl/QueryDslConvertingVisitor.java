package com.contentgrid.thunx.predicates.querydsl;

import com.contentgrid.opa.rego.ast.Term;
import com.contentgrid.thunx.predicates.model.CollectionValue;
import com.contentgrid.thunx.predicates.model.FunctionExpression;
import com.contentgrid.thunx.predicates.model.Scalar;
import com.contentgrid.thunx.predicates.model.SymbolicReference;
import com.contentgrid.thunx.predicates.model.SymbolicReference.PathElement;
import com.contentgrid.thunx.predicates.model.SymbolicReference.PathElementVisitor;
import com.contentgrid.thunx.predicates.model.ThunkExpressionVisitor;
import com.contentgrid.thunx.predicates.model.Variable;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Ops;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.PathBuilder;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import javax.persistence.Embedded;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class QueryDslConvertingVisitor implements ThunkExpressionVisitor<Expression<?>, QueryDslConversionContext> {

    private final PropertyAccessStrategy accessStrategy;

    QueryDslConvertingVisitor() {
        this(new FieldByReflectionAccessStrategy());
    }

    @Override
    public Expression<?> visit(Scalar<?> scalar, QueryDslConversionContext context) {
        if (scalar == Scalar.nullValue()) {
            return Expressions.nullExpression();
        }
        return Expressions.constant(scalar.getValue());
    }

    @Override
    public Expression<?> visit(FunctionExpression<?> function, QueryDslConversionContext context) {

        // convert all the terms
        var terms = function.getTerms().stream()
                .map(term -> term.accept(this, context))
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
            case IN:
                assertTwoTerms(terms);
                return ExpressionUtils.predicate(Ops.IN, terms.get(0), terms.get(1));
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
            throw new IllegalArgumentException("Operation requires 2 parameters.");
        }
    }

    @Override
    public Expression<?> visit(SymbolicReference symbolicReference, QueryDslConversionContext context) {
        String subject = symbolicReference.getSubject().getName();
        if (!"entity".equalsIgnoreCase(subject)) {
            throw new IllegalArgumentException(
                    "Expected symbolic-ref subject named 'entity', but got '" + subject + "'");
        }

        var path = symbolicReference.getPath();
        PathBuilder<?> builder = context.getPathBuilder();
        for (var elem : path) {
            builder = traversePath(symbolicReference, builder, getPathElementName(elem));
        }

        assertNotReferencingEntity(symbolicReference, builder);

        return builder;
    }

    /**
     * Safety check: Because Thunx is only providing the QueryDSL-predicate, it can only use implicit joins. That means
     * the leaf cannot reference a relation/entity, only attributes. This is enforced by checking the corresponding
     * annotated element and check it does not contain any relation or @Embedded
     * annotations
     */
    private static void assertNotReferencingEntity(@NonNull SymbolicReference symbolicReference,
                                                   @NonNull PathBuilder<?> builder) {

        var blacklist = Set.of(OneToOne.class, OneToMany.class, ManyToOne.class, ManyToMany.class, Embedded.class);
        var element = builder.getAnnotatedElement();
        if (blacklist.stream().anyMatch(element::isAnnotationPresent)) {
            var msg = String.format("Cannot use `%s` as an expression, because it refers to a relation, not an attribute.",
                    symbolicReference,
                    element instanceof Field ? ((Field) element).getType().getName() + " " + ((Field) element).getName() : element);

            throw new IllegalArgumentException(msg);
        }
    }

    private PathBuilder<?> traversePath(SymbolicReference symbolicReference, PathBuilder<?> builder,
                                        String pathElement) {

        // we want to build a typed path, making sure the segments in the path are valid
        var property = this.accessStrategy.getProperty(builder.getType(), pathElement).orElseThrow(() -> {
            String msg = String.format("Unknown property '%s' on %s, while traversing %s",
                    pathElement, builder.getType().getSimpleName(), symbolicReference.toPath());
            throw new IllegalArgumentException(msg);

        });

        return builder.get(pathElement, property.getType());
    }

    private static String getPathElementName(PathElement elem) {
        return elem.accept(new PathElementVisitor<>() {
            @Override
            public String visit(Scalar<?> scalar) {
                if (scalar.getResultType().equals(String.class)) {
                    return (String) scalar.getValue();
                } else {
                    var msg = String.format( "cannot traverse symbolic reference using scalar of type %s",
                            scalar.getResultType().getSimpleName());
                    throw new UnsupportedOperationException(msg);
                }
            }

            @Override
            public String visit(Variable variable) {
                return variable.getName();
            }
        });
    }

    @Override
    public Expression<?> visit(Variable variable, QueryDslConversionContext context) {
        // TODO could there be more variables available, than just the subject-path-builder ?
        throw new UnsupportedOperationException("converting variable to querydsl is not yet implemented");
    }

    @Override
    public Expression<?> visit(CollectionValue collection, QueryDslConversionContext context) {
        return Expressions.constant(collection.getValue().stream().map(Scalar::getValue).collect(Collectors.toList()));
    }
}
