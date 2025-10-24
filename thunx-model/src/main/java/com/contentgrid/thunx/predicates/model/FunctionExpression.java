package com.contentgrid.thunx.predicates.model;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

public interface FunctionExpression<T> extends ThunkExpression<T> {

    Operator getOperator();

    List<ThunkExpression<?>> getTerms();

    default <R, C> R accept(ThunkExpressionVisitor<R, C> visitor, C context) {
        return visitor.visit(this, context);
    }

    default String toDebugString() {
        return String.format("%s(%s)",
                this.getOperator().getKey().toUpperCase(Locale.ROOT),
                this.getTerms().stream().map(Object::toString).collect(Collectors.joining(", ")));
    }

    // TASK switch this to an interface Operator<T> with sealed classes as subtypes
    // that would eliminate much of the casting
    @RequiredArgsConstructor
    enum Operator {
        // Comparison operators
        EQUALS("eq", Boolean.class, (FunctionExpressionFactory<Boolean>) Comparison::areEqual),
        NOT_EQUAL_TO("neq", Boolean.class, (FunctionExpressionFactory<Boolean>) Comparison::notEqual),
        GREATER_THAN("gt", Boolean.class, (FunctionExpressionFactory<Boolean>) Comparison::greater),
        GREATER_THAN_OR_EQUAL_TO("gte", Boolean.class, (FunctionExpressionFactory<Boolean>) Comparison::greaterOrEquals),
        LESS_THAN("lt", Boolean.class, (FunctionExpressionFactory<Boolean>) Comparison::less),
        LESS_THEN_OR_EQUAL_TO("lte", Boolean.class, (FunctionExpressionFactory<Boolean>) Comparison::lessOrEquals),
        IN("in", Boolean.class, (FunctionExpressionFactory<Boolean>) Comparison::in ),

        // Logical operator
        AND("and", Boolean.class, (FunctionExpressionFactory<Boolean>) LogicalOperation::uncheckedConjunction),
        OR("or", Boolean.class, (FunctionExpressionFactory<Boolean>) LogicalOperation::uncheckedDisjunction),
        NOT("not", Boolean.class, (FunctionExpressionFactory<Boolean>) LogicalOperation::uncheckedNegation),

        // Numeric operators
        PLUS("plus", Number.class, (FunctionExpressionFactory<Number>) NumericFunction::plus),
        MINUS("minus", Number.class, (FunctionExpressionFactory<Number>) NumericFunction::minus),
        DIVIDE("div", Number.class, (FunctionExpressionFactory<Number>) NumericFunction::divide),
        MULTIPLY("mul", Number.class, (FunctionExpressionFactory<Number>) NumericFunction::multiply),
        MODULUS("mod", Number.class, (FunctionExpressionFactory<Number>) NumericFunction::modulus),

        // Custom operator
        /**
         * Custom operator, to be used in classes that extend {@link FunctionExpression} outside the thunx project.
         * <p>
         * Note: it is not supported to serialize to and deserialize from
         * {@see com.contentgrid.thunx.encoding.json.JsonFunctionDto}
         */
        CUSTOM("custom", Object.class, terms -> {
            throw new IllegalArgumentException("Custom function expressions are not supported.");
        });

        @Getter
        @NonNull
        private final String key;

        @Getter
        @NonNull
        private final Class<?> type;

        // TODO remove getter, expose create method directly and delegate to the factory instead
        @Getter
        @NonNull
        private final FunctionExpressionFactory<?> factory;

        public static Operator resolve(@NonNull String key) {
            return Arrays.stream(Operator.values())
                    .filter(op -> Objects.equals(key, op.getKey()))
                    .findFirst()
                    .orElseThrow(() -> {
                        String message = String.format("Invalid %s key: '%s'", Operator.class.getSimpleName(), key);
                        return new IllegalArgumentException(message);
                    });
        }

        public FunctionExpression<?> create(List<ThunkExpression<?>> terms) {
            return this.factory.create(terms);
        }
    }

    @FunctionalInterface
    interface FunctionExpressionFactory<T> {

        FunctionExpression<T> create(List<ThunkExpression<?>> terms);
    }

}
