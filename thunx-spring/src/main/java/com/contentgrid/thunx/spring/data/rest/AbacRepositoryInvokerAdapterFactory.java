package com.contentgrid.thunx.spring.data.rest;

import com.querydsl.core.types.Predicate;
import lombok.AllArgsConstructor;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.support.Repositories;
import org.springframework.data.repository.support.RepositoryInvoker;
import org.springframework.transaction.PlatformTransactionManager;

@AllArgsConstructor
public class AbacRepositoryInvokerAdapterFactory {

    private Repositories repositories;
    private PlatformTransactionManager transactionManager;

    public RepositoryInvoker createRepositoryInvoker(RepositoryInvoker repositoryInvoker, Class<?> domainType,
            Predicate predicate) {
        var executor = repositories.getRepositoryFor(domainType)
                .map(QuerydslPredicateExecutor.class::cast)
                .orElseThrow();

        var repositoryInformation = this.repositories.getRequiredRepositoryInformation(domainType);
        var persistentEntity = repositories.getPersistentEntity(domainType);
        var entityInformation = repositories.getEntityInformationFor(domainType);

        return new AbacRepositoryInvokerAdapter(repositoryInvoker, executor, predicate, transactionManager,
                repositoryInformation, persistentEntity, entityInformation);
    }

}
