package eu.contentcloud.abac.predicates.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

public interface FunctionExpression<T> extends Expression<T> {

    Operator getOperator();

    List<Expression<?>> getTerms();

    default <R> R accept(ExpressionVisitor<R> visitor) {
        return visitor.visit(this);
    }



    @RequiredArgsConstructor
    @AllArgsConstructor
    enum Operator {
        // Comparison operators
        EQUALS("eq", Boolean.class, (FunctionExpressionFactory<Boolean>) Comparison::areEqual),
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

        @Getter
        private FunctionExpressionFactory factory;

        public static Operator resolve(@NonNull String key) {
            return Arrays.stream(Operator.values())
                    .filter(op -> Objects.equals(key, op.getKey()))
                    .findFirst()
                    .orElseThrow(() -> {
                        String message = String.format("Invalid %s key: '%s'", Operator.class.getSimpleName(), key);
                        return new IllegalArgumentException(message);
                    });
        }
    }

    @FunctionalInterface
    interface FunctionExpressionFactory<T> {
        FunctionExpression<T> create(List<Expression<?>> terms);
    }
}
