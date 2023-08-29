package com.contentgrid.thunx.spring.data.querydsl.predicate.injector.resolver;

import com.contentgrid.thunx.spring.data.querydsl.predicate.injector.repository.RepositoryInvokerAdapterFactory;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

/**
 * Only applies predicates on the collection of entities
 * <p>
 * This implementation of {@link OperationPredicates} is the only implementation that can be used with the out-of-the
 * box {@link RepositoryInvokerAdapterFactory}
 */
@RequiredArgsConstructor
public class CollectionFilteringOperationPredicates implements OperationPredicates {

    private final Predicate predicate;

    @Override
    public OperationPredicates and(OperationPredicates predicate) {
        if (predicate instanceof CollectionFilteringOperationPredicates) {
            return new CollectionFilteringOperationPredicates(ExpressionUtils.and(
                    this.collectionFilterPredicate().orElse(null),
                    predicate.collectionFilterPredicate().orElse(null)
            ));
        }
        return OperationPredicates.super.and(predicate);
    }

    @Override
    public Optional<Predicate> collectionFilterPredicate() {
        return Optional.ofNullable(predicate);
    }

    @Override
    public Optional<Predicate> readPredicate() {
        return Optional.empty();
    }

    @Override
    public Optional<Predicate> afterCreatePredicate() {
        return Optional.empty();
    }

    @Override
    public Optional<Predicate> beforeUpdatePredicate() {
        return Optional.empty();
    }

    @Override
    public Optional<Predicate> afterUpdatePredicate() {
        return Optional.empty();
    }

    @Override
    public Optional<Predicate> beforeDeletePredicate() {
        return Optional.empty();
    }
}
