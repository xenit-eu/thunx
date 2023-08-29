package com.contentgrid.thunx.spring.data.querydsl.predicate.injector.rest.webmvc;

import com.contentgrid.thunx.spring.data.querydsl.predicate.injector.repository.RepositoryInvokerAdapterFactory;
import com.contentgrid.thunx.spring.data.querydsl.predicate.injector.resolver.QuerydslPredicateResolver;
import com.contentgrid.thunx.spring.data.querydsl.predicate.injector.resolver.OperationPredicates;
import java.util.Map;
import java.util.Optional;
import lombok.NonNull;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.MethodParameter;
import org.springframework.data.repository.support.Repositories;
import org.springframework.data.repository.support.RepositoryInvoker;
import org.springframework.data.repository.support.RepositoryInvokerFactory;
import org.springframework.data.rest.webmvc.RootResourceInformation;
import org.springframework.data.rest.webmvc.config.ResourceMetadataHandlerMethodArgumentResolver;
import org.springframework.data.rest.webmvc.config.RootResourceInformationHandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;

/**
 * {@link HandlerMethodArgumentResolver} to create {@link RootResourceInformation} for injection into Spring MVC
 * controller methods.
 * <p>
 * This variant injects a custom {@link RepositoryInvoker} that filters its output based on QueryDSL predicates resolved
 * by {@link QuerydslPredicateResolver}
 */
class PredicateInjectingRootResourceInformationHandlerMethodArgumentResolver extends
        RootResourceInformationHandlerMethodArgumentResolver {

    private final RepositoryInvokerAdapterFactory repositoryInvokerAdapterFactory;
    private final ObjectProvider<QuerydslPredicateResolver> predicateResolvers;

    public PredicateInjectingRootResourceInformationHandlerMethodArgumentResolver(
            Repositories repositories,
            RepositoryInvokerFactory invokerFactory,
            ResourceMetadataHandlerMethodArgumentResolver resourceMetadataResolver,
            @NonNull RepositoryInvokerAdapterFactory repositoryInvokerAdapterFactory,
            @NonNull ObjectProvider<QuerydslPredicateResolver> predicateResolvers
    ) {
        super(repositories, invokerFactory, resourceMetadataResolver);

        this.repositoryInvokerAdapterFactory = repositoryInvokerAdapterFactory;
        this.predicateResolvers = predicateResolvers;
    }

    @Override
    protected RepositoryInvoker postProcess(MethodParameter parameter, RepositoryInvoker invoker, Class<?> domainType, Map<String, String[]> parameters) {
        return getPredicate(parameter, domainType, parameters)
                .map(predicate -> repositoryInvokerAdapterFactory.adaptRepositoryInvoker(invoker, domainType, predicate))
                .orElse(invoker);
    }

    private Optional<OperationPredicates> getPredicate(MethodParameter parameter, Class<?> domainType, Map<String, String[]> parameters) {
        return predicateResolvers.stream()
                .map(resolver -> resolver.resolve(parameter, domainType, parameters))
                .flatMap(Optional::stream)
                .reduce(OperationPredicates::and);
    }
}
