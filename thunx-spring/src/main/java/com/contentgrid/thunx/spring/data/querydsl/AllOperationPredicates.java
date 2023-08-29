package com.contentgrid.thunx.spring.data.querydsl;

import com.contentgrid.thunx.spring.data.querydsl.predicate.injector.resolver.OperationPredicates;
import com.querydsl.core.types.Predicate;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
class AllOperationPredicates implements OperationPredicates {

    private final Optional<Predicate> sharedPredicate;

    public AllOperationPredicates(Predicate sharedPredicate) {
        this(Optional.ofNullable(sharedPredicate));
    }


    @Override
    public Optional<Predicate> collectionFilterPredicate() {
        return sharedPredicate;
    }

    @Override
    public Optional<Predicate> readPredicate() {
        return sharedPredicate;
    }

    @Override
    public Optional<Predicate> afterCreatePredicate() {
        return sharedPredicate;
    }

    @Override
    public Optional<Predicate> beforeUpdatePredicate() {
        return sharedPredicate;
    }

    @Override
    public Optional<Predicate> afterUpdatePredicate() {
        return sharedPredicate;
    }

    @Override
    public Optional<Predicate> beforeDeletePredicate() {
        return sharedPredicate;
    }
}
