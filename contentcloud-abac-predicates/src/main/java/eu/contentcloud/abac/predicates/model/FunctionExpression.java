package eu.contentcloud.abac.predicates.model;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
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
        EQUALS("eq", Boolean.class,  (FunctionExpressionFactory<Boolean>) Comparison::areEqual,
                values -> values.distinct().count() <= 1),
        NOT_EQUAL_TO("neq", Boolean.class),
        GREATER_THAN("gt", Boolean.class),
        GREATER_THAN_OR_EQUAL_TO("gte", Boolean.class),
        LESS_THAN("lt", Boolean.class),
        LESS_THEN_OR_EQUAL_TO("lte", Boolean.class),

        // Logical operator
        AND("and", Boolean.class, (FunctionExpressionFactory<Boolean>) LogicalOperation::uncheckedConjunction,
                values -> {
                    return values.allMatch(Boolean.TRUE::equals);
                }),
        OR("or", Boolean.class, (FunctionExpressionFactory<Boolean>) LogicalOperation::uncheckedDisjunction,
                values -> {
            return values.anyMatch(Boolean.TRUE::equals);
        }),
        NOT("not", Boolean.class),

        // Numeric operators
        PLUS("plus", Number.class),
        MINUS("minus", Number.class),
        DIVIDE("div", Number.class),
        MULTIPLY("mul", Number.class),
        MODULUS("mod", Number.class);

        @Getter
        @NonNull
        private final String key;

        @Getter
        @NonNull
        private final Class<?> type;

        // TODO remove getter, expose create method directly and delegate to the factory instead
        @Getter
        private FunctionExpressionFactory<?> factory;

        private FunctionExpressionEvaluator<?> evaluator;

        public static Operator resolve(@NonNull String key) {
            return Arrays.stream(Operator.values())
                    .filter(op -> Objects.equals(key, op.getKey()))
                    .findFirst()
                    .orElseThrow(() -> {
                        String message = String.format("Invalid %s key: '%s'", Operator.class.getSimpleName(), key);
                        return new IllegalArgumentException(message);
                    });
        }

        public <T> T eval(Stream<Object> values) {
            if (this.evaluator == null) {
                throw new UnsupportedOperationException("Operator '"+this.getKey()+"' does not support eval");
            }
            return (T) this.evaluator.eval(values);
        }

    }

    @FunctionalInterface
    interface FunctionExpressionFactory<T> {

        FunctionExpression create(List<Expression<?>> terms);
    }

    @FunctionalInterface
    interface FunctionExpressionEvaluator<T> {

        T eval(Stream<Object> values);
    }
}
