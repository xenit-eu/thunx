package com.contentgrid.thunx.visitor.reducer;

import com.contentgrid.thunx.predicates.model.ThunkExpression;
import com.contentgrid.thunx.predicates.model.FunctionExpression.FunctionExpressionFactory;
import com.contentgrid.thunx.predicates.model.Scalar;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;


@AllArgsConstructor
class LogicalFunctionReducer implements FunctionReducer<Boolean> {
    private final Boolean forcingTerm;
    private final Boolean identityTerm;
    private final FunctionExpressionFactory<Boolean> factory;

    @Override
    public Optional<ThunkExpression<Boolean>> tryReduce(List<ThunkExpression<?>> values) {
        var hasForcingTerm = values.stream()
                .map(expression -> expression.assertResultType(Boolean.class))
                .flatMap(e -> ThunkExpression.maybeScalar(e).stream())
                .map(Scalar::getValue)
                .anyMatch(Predicate.isEqual(forcingTerm));
        if(hasForcingTerm) {
            return Optional.of(Scalar.of(forcingTerm));
        }
        var withoutIdentityTerms = values.stream()
                .filter(e -> ThunkExpression.maybeScalar(e)
                        .map(Scalar::getValue)
                        .filter(Predicate.isEqual(identityTerm)).isEmpty())
                .collect(Collectors.toList());
        switch (withoutIdentityTerms.size()) {
            case 0:
                return Optional.of(Scalar.of(identityTerm));
            case 1:
                return Optional.of((ThunkExpression<Boolean>)withoutIdentityTerms.get(0));
            default:
                return Optional.of(factory.create(withoutIdentityTerms));
        }
    }
}

