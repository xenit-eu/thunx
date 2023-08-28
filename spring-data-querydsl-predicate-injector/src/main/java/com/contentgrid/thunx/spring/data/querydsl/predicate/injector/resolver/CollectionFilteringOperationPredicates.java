package com.contentgrid.thunx.spring.data.querydsl.predicate.injector.resolver;

import com.contentgrid.thunx.spring.data.querydsl.predicate.injector.repository.RepositoryInvokerAdapterFactory;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
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
                    this.collectionFilterPredicate(),
                    predicate.collectionFilterPredicate()
            ));
        }
        return OperationPredicates.super.and(predicate);
    }

    @Override
    public Predicate collectionFilterPredicate() {
        return predicate;
    }

    @Override
    public Predicate readPredicate() {
        return null;
    }

    @Override
    public Predicate afterCreatePredicate() {
        return null;
    }

    @Override
    public Predicate beforeUpdatePredicate() {
        return null;
    }

    @Override
    public Predicate afterUpdatePredicate() {
        return null;
    }

    @Override
    public Predicate beforeDeletePredicate() {
        return null;
    }
}
