package com.contentgrid.thunx.spring.security;

import com.contentgrid.thunx.predicates.model.LogicalOperation;
import com.contentgrid.thunx.spring.data.context.AbacContextSupplier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.SecurityContextHolder;

@Configuration
public class JwtAbacConfiguration {

    @Bean
    public AbacContextSupplier jwtAbacContextSupplier() {
        return () -> SecurityContextHolder.getContext()
                .getAuthentication()
                .getAuthorities()
                .stream()
                .filter(authority -> authority instanceof AbacContextAuthority)
                .map(AbacContextAuthority.class::cast)
                .map(AbacContextAuthority::getExpression)
                .reduce(LogicalOperation::disjunction)
                .orElse(null);
    }
}
