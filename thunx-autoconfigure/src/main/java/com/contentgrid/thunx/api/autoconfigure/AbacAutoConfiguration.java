package com.contentgrid.thunx.api.autoconfigure;

import com.contentgrid.thunx.spring.data.querydsl.AbacQuerydslPredicateResolver;
import com.contentgrid.thunx.spring.data.querydsl.predicate.injector.resolver.QuerydslPredicateResolver;
import com.contentgrid.thunx.spring.data.querydsl.predicate.injector.rest.webmvc.SpringDataQuerydslPredicateInjectorAutoConfiguration;
import com.contentgrid.thunx.spring.data.rest.AbacConfiguration;
import com.contentgrid.thunx.spring.data.rest.HttpHeaderAbacConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.boot.autoconfigure.data.rest.RepositoryRestMvcAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.querydsl.binding.QuerydslBindingsFactory;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@AutoConfiguration(
        before = SpringDataQuerydslPredicateInjectorAutoConfiguration.class,
        after = RepositoryRestMvcAutoConfiguration.class
)
@ConditionalOnClass(RepositoryRestResource.class)
@ConditionalOnWebApplication(type = Type.SERVLET)
public class AbacAutoConfiguration {

    @Import(AbacConfiguration.class)
    public static class EnableAbacAutoConfiguration {
    }

    @ConditionalOnProperty(value = "contentgrid.thunx.abac.source", havingValue = "header", matchIfMissing = true)
    @Import(HttpHeaderAbacConfiguration.class)
    public static class HttpHeaderAbacAutoConfiguration {
    }

    @ConditionalOnProperty(value = "contentgrid.thunx.abac.source", havingValue = "none")
    public static class NoneAbacAutoConfiguration {
        // Only when 'contentgrid.thunx.abac.source' equals 'none', the abac context checking is disabled

        @Bean
        public QuerydslPredicateResolver abacQuerydslPredicateResolver(QuerydslBindingsFactory querydslBindingsFactory) {
            return new AbacQuerydslPredicateResolver(querydslBindingsFactory.getEntityPathResolver(), () -> null);
        }
    }
}
