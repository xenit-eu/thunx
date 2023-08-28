package com.contentgrid.thunx.spring.data.querydsl.predicate.injector.rest.webmvc;

import com.contentgrid.thunx.spring.data.querydsl.predicate.injector.resolver.QuerydslPredicateResolver;
import com.contentgrid.thunx.spring.data.querydsl.predicate.injector.resolver.OperationPredicates;
import com.contentgrid.thunx.spring.data.querydsl.predicate.injector.resolver.CollectionFilteringOperationPredicates;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import org.springframework.core.MethodParameter;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.querydsl.binding.QuerydslBindingsFactory;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.data.querydsl.binding.QuerydslPredicateBuilder;
import org.springframework.data.util.TypeInformation;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * Resolves the QueryDSL Predicate from request query parameters using bindings provided with
 * {@link QuerydslBindingsFactory}.
 * <p>
 * This is equivalent to the built-in functionality of Spring DATA REST with QueryDSL integration
 */
public class QuerydslBindingsPredicateResolver implements QuerydslPredicateResolver {

    private final QuerydslPredicateBuilder predicateBuilder;
    private final QuerydslBindingsFactory querydslBindingsFactory;

    QuerydslBindingsPredicateResolver(ConversionService conversionService,
            QuerydslBindingsFactory querydslBindingsFactory) {
        this.predicateBuilder = new QuerydslPredicateBuilder(
                conversionService,
                querydslBindingsFactory.getEntityPathResolver()
        );
        this.querydslBindingsFactory = querydslBindingsFactory;
    }

    @Override
    public Optional<OperationPredicates> resolve(MethodParameter methodParameter, Class<?> domainType,
            Map<String, String[]> parameters) {
        if (!methodParameter.hasParameterAnnotation(QuerydslPredicate.class)) {
            return Optional.empty();
        }

        var domainTypeInfo = TypeInformation.of(domainType);
        var bindings = querydslBindingsFactory.createBindingsFor(domainTypeInfo);

        return Optional.of(predicateBuilder.getPredicate(domainTypeInfo, toMultiValueMap(parameters), bindings))
                .map(CollectionFilteringOperationPredicates::new);
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
