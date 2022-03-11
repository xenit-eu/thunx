package eu.xenit.contentcloud.thunx.spring.data.rest;

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

    public RepositoryInvoker createRepositoryInvoker(RepositoryInvoker repositoryInvoker, Class<?> domainType, Predicate predicate) {
        QuerydslPredicateExecutor<Object> predicateExecutor = repositories.getRepositoryFor(domainType)
                .map(QuerydslPredicateExecutor.class::cast)
                .orElseThrow();

        return new AbacRepositoryInvokerAdapter(repositoryInvoker, predicateExecutor, transactionManager, domainType, predicate);
    }

}
