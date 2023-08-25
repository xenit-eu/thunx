package com.contentgrid.thunx.spring.data.querydsl.predicate.injector.resolver;

import com.querydsl.core.types.Predicate;
import java.util.Map;
import java.util.Optional;
import org.springframework.core.MethodParameter;

/**
 * Resolves a QueryDSL {@link Predicate} for injection into a method parameter of a Spring controller
 */
public interface QuerydslPredicateResolver {

    /**
     * Resolve a QueryDSL {@link Predicate} to apply to requests
     *
     * @param methodParameter Controller parameter to resolve the predicate for
     * @param domainType The spring-data entity class to apply the predicates to
     * @param parameters All parameters parsed from the requests's query string
     * @return The predicate to apply to the request
     */
    Optional<OperationPredicates> resolve(MethodParameter methodParameter, Class<?> domainType, Map<String, String[]> parameters);
}
