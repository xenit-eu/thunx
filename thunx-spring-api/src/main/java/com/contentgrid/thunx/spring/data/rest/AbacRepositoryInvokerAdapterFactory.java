package com.contentgrid.thunx.spring.data.rest;

import com.contentgrid.thunx.predicates.querydsl.PathBuilderFactory;
import com.contentgrid.thunx.spring.data.querydsl.EntityPathResolverBasedPathBuilderFactory;
import com.contentgrid.thunx.spring.data.querydsl.predicate.injector.resolver.OperationPredicates;
import com.contentgrid.thunx.spring.data.querydsl.predicate.injector.repository.RepositoryInvokerAdapterFactory;
import lombok.AllArgsConstructor;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.querydsl.EntityPathResolver;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.support.Repositories;
import org.springframework.data.repository.support.RepositoryInvoker;
import org.springframework.transaction.PlatformTransactionManager;

@AllArgsConstructor
class AbacRepositoryInvokerAdapterFactory implements RepositoryInvokerAdapterFactory {

    private final Repositories repositories;
    private final PlatformTransactionManager transactionManager;
    private final PathBuilderFactory pathBuilderFactory;
    private final ConversionService conversionService;

    public AbacRepositoryInvokerAdapterFactory(
            Repositories repositories,
            PlatformTransactionManager transactionManager,
            EntityPathResolver entityPathResolver,
            ConversionService conversionService
    ) {
        this(repositories, transactionManager, new EntityPathResolverBasedPathBuilderFactory(entityPathResolver),
                conversionService);
    }

    @Override
    public RepositoryInvoker adaptRepositoryInvoker(RepositoryInvoker repositoryInvoker, Class<?> domainType,
            OperationPredicates predicate) {
        var executor = repositories.getRepositoryFor(domainType)
                .map(QuerydslPredicateExecutor.class::cast)
                .orElseThrow();

        var repositoryInformation = this.repositories.getRequiredRepositoryInformation(domainType);
        var persistentEntity = repositories.getPersistentEntity(domainType);
        var entityInformation = repositories.getEntityInformationFor(domainType);

        return new AbacRepositoryInvokerAdapter(repositoryInvoker, executor, predicate, transactionManager,
                repositoryInformation, persistentEntity, entityInformation, pathBuilderFactory.create(domainType),
                conversionService);
    }

}
