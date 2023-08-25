package com.contentgrid.thunx.spring.data.querydsl.predicate.injector.resolver;

import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;

@RequiredArgsConstructor
class CompositeOperationPredicates implements OperationPredicates {
    private final List<OperationPredicates> predicates;

    @Override
    public OperationPredicates and(OperationPredicates predicate) {
        var copy = new ArrayList<>(predicates);
        copy.add(predicate);
        return new CompositeOperationPredicates(copy);
    }

    @Nullable
    private Predicate combine(Function<OperationPredicates, Predicate> extractor) {
        return predicates.stream()
                .map(extractor)
                .reduce(ExpressionUtils::and)
                .orElse(null);
    }

    @Override
    public Predicate readPredicate() {
        return combine(OperationPredicates::readPredicate);
    }

    @Override
    public Predicate afterCreatePredicate() {
        return combine(OperationPredicates::afterCreatePredicate);
    }

    @Override
    public Predicate beforeUpdatePredicate() {
        return combine(OperationPredicates::beforeUpdatePredicate);
    }

    @Override
    public Predicate afterUpdatePredicate() {
        return combine(OperationPredicates::afterUpdatePredicate);
    }

    @Override
    public Predicate beforeDeletePredicate() {
        return combine(OperationPredicates::beforeDeletePredicate);
    }
}
