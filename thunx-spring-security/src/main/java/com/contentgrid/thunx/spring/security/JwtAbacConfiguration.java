package com.contentgrid.thunx.spring.security;

import com.contentgrid.thunx.predicates.model.LogicalOperation;
import com.contentgrid.thunx.predicates.model.ThunkExpression;
import com.contentgrid.thunx.spring.data.querydsl.AbacQuerydslPredicateResolver;
import com.contentgrid.thunx.spring.data.querydsl.predicate.injector.resolver.QuerydslPredicateResolver;
import java.util.function.Supplier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.querydsl.binding.QuerydslBindingsFactory;
import org.springframework.security.core.context.SecurityContextHolder;

@Configuration
public class JwtAbacConfiguration {

    @Bean
    public QuerydslPredicateResolver abacQuerydslPredicateResolver(QuerydslBindingsFactory querydslBindingsFactory) {
        Supplier<ThunkExpression<Boolean>> supplier = () -> SecurityContextHolder.getContext()
                .getAuthentication()
                .getAuthorities()
                .stream()
                .filter(authority -> authority instanceof AbacContextAuthority)
                .map(AbacContextAuthority.class::cast)
                .map(AbacContextAuthority::getExpression)
                .reduce(LogicalOperation::disjunction)
                .orElse(null);

        return new AbacQuerydslPredicateResolver(querydslBindingsFactory.getEntityPathResolver(), supplier);
    }


}
