package com.contentgrid.thunx.spring.data.rest;

import com.querydsl.core.types.Predicate;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import org.springframework.core.MethodParameter;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.AbacQuerydslPredicateBuilder;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.data.querydsl.binding.QuerydslBindingsFactory;
import org.springframework.data.repository.support.Repositories;
import org.springframework.data.repository.support.RepositoryInvoker;
import org.springframework.data.repository.support.RepositoryInvokerFactory;
import org.springframework.data.rest.webmvc.config.ResourceMetadataHandlerMethodArgumentResolver;
import org.springframework.data.rest.webmvc.config.RootResourceInformationHandlerMethodArgumentResolver;
import org.springframework.data.util.ClassTypeInformation;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

public class AbacRootResourceInformationHandlerMethodArgumentResolver
    extends RootResourceInformationHandlerMethodArgumentResolver {

    private final Repositories repositories;
    private final AbacQuerydslPredicateBuilder predicateBuilder;
    private final QuerydslBindingsFactory querydslBindingsFactory;
    private final AbacRepositoryInvokerAdapterFactory repositoryInvokerFactory;

    /**
     * Creates a new {@link AbacRootResourceInformationHandlerMethodArgumentResolver} using the given
     * {@link Repositories}, {@link RepositoryInvokerFactory} and {@link ResourceMetadataHandlerMethodArgumentResolver}.
     *
     * @param repositories must not be {@literal null}.
     * @param invokerFactory must not be {@literal null}.
     * @param resourceMetadataResolver must not be {@literal null}.
     * @param predicateBuilder must not be {@literal null}.
     * @param querydslBindingsFactory must not be {@literal null}.
     * @param repositoryInvokerFactory must not be {@literal null}.
     */
    public AbacRootResourceInformationHandlerMethodArgumentResolver(
            Repositories repositories,
            RepositoryInvokerFactory invokerFactory,
            ResourceMetadataHandlerMethodArgumentResolver resourceMetadataResolver,
            AbacQuerydslPredicateBuilder predicateBuilder,
            QuerydslBindingsFactory querydslBindingsFactory,
            AbacRepositoryInvokerAdapterFactory repositoryInvokerFactory) {

        super(repositories, invokerFactory, resourceMetadataResolver);

        Assert.notNull(repositories, "Repositories must not be null!");
        Assert.notNull(predicateBuilder, "predicateBuilder must not be null!");
        Assert.notNull(querydslBindingsFactory, "querydslBindingsFactory must not be null!");
        Assert.notNull(repositoryInvokerFactory, "repositoryInvokerFactory must not be null!");

        this.repositories = repositories;
        this.predicateBuilder = predicateBuilder;
        this.querydslBindingsFactory = querydslBindingsFactory;
        this.repositoryInvokerFactory = repositoryInvokerFactory;
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
                .flatMap(executor -> getPredicate(domainType, parameters))
                .map(predicate -> repositoryInvokerFactory.createRepositoryInvoker(invoker, domainType, predicate))
                .orElse(invoker);
    }

    private Optional<Predicate> getPredicate(Class<?> domainType, Map<String, String[]> parameters) {

        ClassTypeInformation<?> type = ClassTypeInformation.from(domainType);
        QuerydslBindings bindings = querydslBindingsFactory.createBindingsFor(type);
        Predicate predicate = predicateBuilder.getPredicate(type, toMultiValueMap(parameters), bindings);

        return Optional.ofNullable(predicate);
    }

    /**
     * Converts the given Map into a {@link MultiValueMap}.
     *
     * @param source must not be {@literal null}.
     * @return the converted {@link MultiValueMap}.
     */
    private static MultiValueMap<String, String> toMultiValueMap(Map<String, String[]> source) {

        MultiValueMap<String, String> result = new LinkedMultiValueMap<>();

        for (Entry<String, String[]> entry : source.entrySet()) {
            result.put(entry.getKey(), Arrays.asList(entry.getValue()));
        }

        return result;
    }
}
