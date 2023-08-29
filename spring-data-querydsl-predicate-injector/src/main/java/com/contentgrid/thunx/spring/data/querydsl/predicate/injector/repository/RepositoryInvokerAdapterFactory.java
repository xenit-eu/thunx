package com.contentgrid.thunx.spring.data.querydsl.predicate.injector.repository;

import com.contentgrid.thunx.spring.data.querydsl.predicate.injector.resolver.OperationPredicates;
import com.querydsl.core.types.Predicate;
import org.springframework.data.repository.support.RepositoryInvoker;

/**
 * Adapts a {@link RepositoryInvoker} with one applies the {@link Predicate} to results
 */
@FunctionalInterface
public interface RepositoryInvokerAdapterFactory {

    /**
     * Adapt a repository invoker to apply the supplied QueryDSL predicate
     *
     * @param repositoryInvoker Original invoker
     * @param domainType Domain class for which the invoker is requested
     * @param predicate Predicate to apply to the invoker
     * @return An invoker that applies the predicate
     */
    RepositoryInvoker adaptRepositoryInvoker(RepositoryInvoker repositoryInvoker, Class<?> domainType, OperationPredicates predicate);
}
