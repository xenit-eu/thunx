package com.contentgrid.thunx.spring.data.querydsl.predicate.injector.resolver;

import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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

    private Optional<Predicate> combine(Function<OperationPredicates, Optional<Predicate>> extractor) {
        return predicates.stream()
                .map(extractor)
                .flatMap(Optional::stream)
                .reduce(ExpressionUtils::and);
    }

    @Override
    public Optional<Predicate> collectionFilterPredicate() {
        return combine(OperationPredicates::collectionFilterPredicate);
    }

    @Override
    public Optional<Predicate> readPredicate() {
        return combine(OperationPredicates::readPredicate);
    }

    @Override
    public Optional<Predicate> afterCreatePredicate() {
        return combine(OperationPredicates::afterCreatePredicate);
    }

    @Override
    public Optional<Predicate> beforeUpdatePredicate() {
        return combine(OperationPredicates::beforeUpdatePredicate);
    }

    @Override
    public Optional<Predicate> afterUpdatePredicate() {
        return combine(OperationPredicates::afterUpdatePredicate);
    }

    @Override
    public Optional<Predicate> beforeDeletePredicate() {
        return combine(OperationPredicates::beforeDeletePredicate);
    }
}
