package com.contentgrid.thunx.spring.data.rest;

import com.contentgrid.thunx.spring.data.querydsl.AbacQuerydslPredicateBuilder;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import org.springframework.core.MethodParameter;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBindingsFactory;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.data.querydsl.binding.QuerydslPredicateBuilder;
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
    private final QuerydslPredicateBuilder querydslPredicateBuilder;
    private final AbacQuerydslPredicateBuilder abacPredicateBuilder;
    private final QuerydslBindingsFactory querydslBindingsFactory;
    private final AbacRepositoryInvokerAdapterFactory repositoryInvokerFactory;

    /**
     * Creates a new {@link AbacRootResourceInformationHandlerMethodArgumentResolver} using the given
     * {@link Repositories}, {@link RepositoryInvokerFactory} and
     * {@link ResourceMetadataHandlerMethodArgumentResolver}.
     *
     * @param repositories must not be {@literal null}.
     * @param invokerFactory must not be {@literal null}.
     * @param resourceMetadataResolver must not be {@literal null}.
     * @param abacPredicateBuilder must not be {@literal null}.
     * @param querydslBindingsFactory must not be {@literal null}.
     * @param repositoryInvokerFactory must not be {@literal null}.
     */
    public AbacRootResourceInformationHandlerMethodArgumentResolver(
            Repositories repositories,
            RepositoryInvokerFactory invokerFactory,
            ResourceMetadataHandlerMethodArgumentResolver resourceMetadataResolver,
            QuerydslPredicateBuilder querydslPredicateBuilder, AbacQuerydslPredicateBuilder abacPredicateBuilder,
            QuerydslBindingsFactory querydslBindingsFactory,
            AbacRepositoryInvokerAdapterFactory repositoryInvokerFactory) {

        super(repositories, invokerFactory, resourceMetadataResolver);
        this.querydslPredicateBuilder = querydslPredicateBuilder;

        Assert.notNull(repositories, "Repositories must not be null!");
        Assert.notNull(abacPredicateBuilder, "predicateBuilder must not be null!");
        Assert.notNull(querydslBindingsFactory, "querydslBindingsFactory must not be null!");
        Assert.notNull(repositoryInvokerFactory, "repositoryInvokerFactory must not be null!");

        this.repositories = repositories;
        this.abacPredicateBuilder = abacPredicateBuilder;
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
                .flatMap(executor -> getPredicate(parameter, domainType, parameters))
                .map(predicate -> repositoryInvokerFactory.createRepositoryInvoker(invoker, domainType, predicate))
                .orElse(invoker);
    }

    private Optional<Predicate> getPredicate(MethodParameter parameter, Class<?> domainType, Map<String, String[]> parameters) {

        ClassTypeInformation<?> type = ClassTypeInformation.from(domainType);
        Predicate abacPredicate = abacPredicateBuilder.getPredicate(type);
        var bindings = querydslBindingsFactory.createBindingsFor(type);
        Predicate querydslPredicate = parameter.hasParameterAnnotation(QuerydslPredicate.class)?
                querydslPredicateBuilder.getPredicate(type, toMultiValueMap(parameters), bindings):
                null;

        return Optional.ofNullable(ExpressionUtils.allOf(abacPredicate, querydslPredicate));
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
