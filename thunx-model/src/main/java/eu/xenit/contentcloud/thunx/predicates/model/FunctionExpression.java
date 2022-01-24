package eu.xenit.contentcloud.thunx.predicates.model;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

public interface FunctionExpression<T> extends ThunkExpression<T> {

    Operator getOperator();

    List<ThunkExpression<?>> getTerms();

    default <R> R accept(ThunkExpressionVisitor<R> visitor) {
        return visitor.visit(this);
    }

    default String toDebugString() {
        return String.format("%s(%s)",
                this.getOperator().getKey().toUpperCase(Locale.ROOT),
                this.getTerms().stream().map(Object::toString).collect(Collectors.joining(", ")));
    }

    @Override
    default ThunkExpression<T> simplify() {
        return getOperator()
                .trySimplify(getResultType(), getTerms().stream().map(ThunkExpression::simplify).collect(Collectors.toList()))
                .map(thunkExpression -> (ThunkExpression<T>) thunkExpression)
                .orElse(this);
    }

    @RequiredArgsConstructor
    @AllArgsConstructor
    enum Operator {
        // Comparison operators
        EQUALS("eq", Boolean.class, (FunctionExpressionFactory<Boolean>) Comparison::areEqual,
                (FunctionSimplifier<Boolean>) values -> {
                    var availableValues = values.stream()
                            .map(ResolvedExpression::maybeResolvedExpression)
                            .collect(Collectors.toList());
                    if(availableValues.stream().allMatch(Optional::isPresent)) {
                        return Optional.of(ResolvedExpression.always(
                                availableValues
                                        .stream()
                                        .map(Optional::get)
                                        .map(ResolvedExpression::getResult)
                                        .distinct()
                                        .count() <= 1
                        ));
                    }
                    return Optional.empty();
                }),
        NOT_EQUAL_TO("neq", Boolean.class),
        GREATER_THAN("gt", Boolean.class),
        GREATER_THAN_OR_EQUAL_TO("gte", Boolean.class),
        LESS_THAN("lt", Boolean.class),
        LESS_THEN_OR_EQUAL_TO("lte", Boolean.class),

        // Logical operator
        AND("and", Boolean.class, (FunctionExpressionFactory<Boolean>) LogicalOperation::uncheckedConjunction,
                new LogicalSimplifier(false, true, LogicalOperation::uncheckedConjunction)),
        OR("or", Boolean.class, (FunctionExpressionFactory<Boolean>) LogicalOperation::uncheckedDisjunction,
                new LogicalSimplifier(true, false, LogicalOperation::uncheckedDisjunction)),
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

        @NonNull
        private FunctionSimplifier<?> simplifier = values -> Optional.empty();

        public static Operator resolve(@NonNull String key) {
            return Arrays.stream(Operator.values())
                    .filter(op -> Objects.equals(key, op.getKey()))
                    .findFirst()
                    .orElseThrow(() -> {
                        String message = String.format("Invalid %s key: '%s'", Operator.class.getSimpleName(), key);
                        return new IllegalArgumentException(message);
                    });
        }

        public <T> Optional<ThunkExpression<T>> trySimplify(Class<T> type, List<ThunkExpression<?>> values) {
            return (Optional<ThunkExpression<T>>) (Optional) simplifier.trySimplify(values);
        }

        @AllArgsConstructor
        private static class LogicalSimplifier implements FunctionSimplifier<Boolean> {
            private final Boolean forcingTerm;
            private final Boolean identityTerm;
            private final FunctionExpressionFactory<Boolean> factory;

            @Override
            public Optional<ThunkExpression<Boolean>> trySimplify(List<ThunkExpression<?>> values) {
                var hasForcingTerm = values.stream()
                        .flatMap(e -> ResolvedExpression.maybeResolvedExpression(e).stream())
                        .map(ResolvedExpression::getResult)
                        .anyMatch(Predicate.isEqual(forcingTerm));
                if(hasForcingTerm) {
                    return Optional.of(ResolvedExpression.always(forcingTerm));
                }
                var withoutIdentityTerms = values.stream()
                        .filter(e -> ResolvedExpression.maybeResolvedExpression(e)
                                .map(ResolvedExpression::getResult)
                                .filter(Predicate.isEqual(identityTerm)).isEmpty())
                        .collect(Collectors.toList());
                switch (withoutIdentityTerms.size()) {
                    case 0:
                        return Optional.of(ResolvedExpression.always(identityTerm));
                    case 1:
                        return Optional.of((ThunkExpression<Boolean>)withoutIdentityTerms.get(0));
                    default:
                        return Optional.of(factory.create(withoutIdentityTerms));
                }
            }
        }

    }

    @FunctionalInterface
    interface FunctionExpressionFactory<T> {

        FunctionExpression<T> create(List<ThunkExpression<?>> terms);
    }

    @FunctionalInterface
    interface FunctionSimplifier<T> {

        Optional<ThunkExpression<T>> trySimplify(List<ThunkExpression<?>> values);
    }
}
