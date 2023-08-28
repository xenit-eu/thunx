package com.contentgrid.thunx.spring.data.querydsl.predicate.injector.rest.webmvc;

import com.contentgrid.thunx.spring.data.querydsl.predicate.injector.repository.RepositoryInvokerAdapterFactory;
import com.contentgrid.thunx.spring.data.querydsl.predicate.injector.resolver.OperationPredicates;
import com.contentgrid.thunx.spring.data.querydsl.predicate.injector.resolver.CollectionFilteringOperationPredicates;
import com.querydsl.core.types.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.QuerydslRepositoryInvokerAdapter;
import org.springframework.data.repository.support.Repositories;
import org.springframework.data.repository.support.RepositoryInvoker;

/**
 * Default {@link RepositoryInvokerAdapterFactory} that only processes {@link CollectionFilteringOperationPredicates}
 * with the Spring Data REST {@link QuerydslRepositoryInvokerAdapter}.
 */
@RequiredArgsConstructor
public class QuerydslRepositoryInvokerAdapterFactory implements RepositoryInvokerAdapterFactory {

    private final Repositories repositories;

    @Override
    public RepositoryInvoker adaptRepositoryInvoker(RepositoryInvoker invoker, Class<?> domainType,
            OperationPredicates predicate) {
        return repositories.getRepositoryFor(domainType)
                .filter(QuerydslPredicateExecutor.class::isInstance)
                .map(QuerydslPredicateExecutor.class::cast)
                .<RepositoryInvoker>map(
                        it -> new QuerydslRepositoryInvokerAdapter(invoker, it, unwrapQuerydslPredicates(predicate)))
                .orElse(invoker);
    }

    private Predicate unwrapQuerydslPredicates(OperationPredicates operationPredicates) {
        if (operationPredicates instanceof CollectionFilteringOperationPredicates) {
            return operationPredicates.collectionFilterPredicate();
        }

        throw new IllegalArgumentException(
                "QuerydslRepositoryInvokerAdapter only supports %s, but received %s"
                        .formatted(CollectionFilteringOperationPredicates.class, operationPredicates.getClass())
        );
    }
}
