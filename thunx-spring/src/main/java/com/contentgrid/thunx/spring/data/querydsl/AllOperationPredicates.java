package com.contentgrid.thunx.spring.data.querydsl;

import com.contentgrid.thunx.spring.data.querydsl.predicate.injector.resolver.OperationPredicates;
import com.querydsl.core.types.Predicate;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class AllOperationPredicates implements OperationPredicates {
    private final Predicate sharedPredicate;

    @Override
    public Predicate readPredicate() {
        return sharedPredicate;
    }

    @Override
    public Predicate afterCreatePredicate() {
        return sharedPredicate;
    }

    @Override
    public Predicate beforeUpdatePredicate() {
        return sharedPredicate;
    }

    @Override
    public Predicate afterUpdatePredicate() {
        return sharedPredicate;
    }

    @Override
    public Predicate beforeDeletePredicate() {
        return sharedPredicate;
    }
}
