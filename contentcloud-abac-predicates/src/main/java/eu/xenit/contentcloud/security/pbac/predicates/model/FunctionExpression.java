package eu.xenit.contentcloud.security.pbac.predicates.model;

import java.util.Collection;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

public interface FunctionExpression<T> extends Expression<T> {

    Operator getOperator();

    Collection<Expression<?>> getTerms();

    default <R> R accept(ExpressionVisitor<R> visitor) {
        return visitor.visit(this);
    }



    @RequiredArgsConstructor
    enum Operator {
        // Comparison operators
        EQUALS("eq", Boolean.class),
        NOT_EQUAL_TO("neq", Boolean.class),
        GREATER_THAN("gt", Boolean.class),
        GREATER_THAN_OR_EQUAL_TO("gte", Boolean.class),
        LESS_THAN("lt", Boolean.class),
        LESS_THEN_OR_EQUAL_TO("lte", Boolean.class),

        // Logical operator
        AND("and", Boolean.class),
        OR("or", Boolean.class),
        NOT("not", Boolean.class),

        // Numeric operators
        PLUS("plus", Number.class),
        MINUS("minus", Number.class),
        DIVIDE("div", Number.class),
        MULTIPLY("mul", Number.class),
        MODULUS("mod", Number.class)
        ;

        @Getter
        @NonNull
        private final String key;

        @Getter
        @NonNull
        private final Class<?> type;
    }
}
