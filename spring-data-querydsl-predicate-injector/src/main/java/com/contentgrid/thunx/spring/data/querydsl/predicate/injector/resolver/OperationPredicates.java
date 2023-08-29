package com.contentgrid.thunx.spring.data.querydsl.predicate.injector.resolver;

import com.querydsl.core.types.Predicate;
import java.util.List;
import java.util.Optional;
import org.springframework.lang.Nullable;

/**
 * A set of QueryDSL predicates that apply to different operations that can be executed on an entity
 */
public interface OperationPredicates {

    /**
     * Combines this predicate with a different predicate
     *
     * @param predicate The predicate to AND together with
     * @return The combined predicate
     */
    default OperationPredicates and(OperationPredicates predicate) {
        return new CompositeOperationPredicates(List.of(this, predicate));
    }

    /**
     * @return Predicate used for filtering a collection of entities
     */
    Optional<Predicate> collectionFilterPredicate();

    /**
     * @return Predicate used for reading a single entity
     */
    Optional<Predicate> readPredicate();

    /**
     * @return Predicate used to check permissions for creating an entity with certain values
     */
    Optional<Predicate> afterCreatePredicate();

    /**
     * @return Predicate used to check permission before updating an entity
     */
    Optional<Predicate> beforeUpdatePredicate();

    /**
     * @return Predicate used to check permissions for updating an entity with certain values
     */
    Optional<Predicate> afterUpdatePredicate();

    /**
     * @return Predicate used for deleting an entity
     */
    Optional<Predicate> beforeDeletePredicate();
}
