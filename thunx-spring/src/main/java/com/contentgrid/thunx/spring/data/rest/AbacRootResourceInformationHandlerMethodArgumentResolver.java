package com.contentgrid.thunx.spring.data.rest;

import com.contentgrid.thunx.spring.data.querydsl.QuerydslPredicateResolver;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.MethodParameter;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.support.Repositories;
import org.springframework.data.repository.support.RepositoryInvoker;
import org.springframework.data.repository.support.RepositoryInvokerFactory;
import org.springframework.data.rest.webmvc.config.ResourceMetadataHandlerMethodArgumentResolver;
import org.springframework.data.rest.webmvc.config.RootResourceInformationHandlerMethodArgumentResolver;
import org.springframework.util.Assert;

public class AbacRootResourceInformationHandlerMethodArgumentResolver
    extends RootResourceInformationHandlerMethodArgumentResolver {

    private final Repositories repositories;
    private final ObjectProvider<QuerydslPredicateResolver> predicateResolvers;
    private final AbacRepositoryInvokerAdapterFactory repositoryInvokerFactory;

    /**
     * Creates a new {@link AbacRootResourceInformationHandlerMethodArgumentResolver} using the given
     * {@link Repositories}, {@link RepositoryInvokerFactory} and
     * {@link ResourceMetadataHandlerMethodArgumentResolver}.
     *
     * @param repositories must not be {@literal null}.
     * @param invokerFactory must not be {@literal null}.
     * @param resourceMetadataResolver must not be {@literal null}.
     * @param repositoryInvokerFactory must not be {@literal null}.
     */
    public AbacRootResourceInformationHandlerMethodArgumentResolver(
            Repositories repositories,
            RepositoryInvokerFactory invokerFactory,
            ResourceMetadataHandlerMethodArgumentResolver resourceMetadataResolver,
            ObjectProvider<QuerydslPredicateResolver> predicateResolvers,
            AbacRepositoryInvokerAdapterFactory repositoryInvokerFactory) {

        super(repositories, invokerFactory, resourceMetadataResolver);

        Assert.notNull(repositories, "Repositories must not be null!");
        Assert.notNull(repositoryInvokerFactory, "repositoryInvokerFactory must not be null!");

        this.repositories = repositories;
        this.repositoryInvokerFactory = repositoryInvokerFactory;
        this.predicateResolvers = predicateResolvers;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.rest.webmvc.config.RootResourceInformationHandlerMethodArgumentResolver#postProcess(org.springframework.data.repository.support.RepositoryInvoker, java.lang.Class, java.util.Map)
     */
    @Override
    protected RepositoryInvoker postProcess(MethodParameter parameter, RepositoryInvoker invoker, Class<?> domainType,
            Map<String, String[]> parameters) {

        return repositories.getRepositoryFor(domainType)
                .filter(QuerydslPredicateExecutor.class::isInstance)
                .flatMap(executor -> getPredicate(parameter, domainType, parameters))
                .map(predicate -> repositoryInvokerFactory.createRepositoryInvoker(invoker, domainType, predicate))
                .orElse(invoker);
    }

    private Optional<Predicate> getPredicate(MethodParameter parameter, Class<?> domainType, Map<String, String[]> parameters) {
        return predicateResolvers.stream()
                .map(resolver -> resolver.resolve(parameter, domainType, parameters))
                .flatMap(Optional::stream)
                .reduce(ExpressionUtils::and);
    }

}
