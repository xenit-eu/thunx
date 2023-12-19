package com.contentgrid.thunx.api.autoconfigure;

import com.contentgrid.thunx.spring.data.EnableAbac;
import com.contentgrid.thunx.spring.data.querydsl.predicate.injector.rest.webmvc.SpringDataQuerydslPredicateInjectorAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.boot.autoconfigure.data.rest.RepositoryRestMvcAutoConfiguration;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@AutoConfiguration(
        before = SpringDataQuerydslPredicateInjectorAutoConfiguration.class,
        after = RepositoryRestMvcAutoConfiguration.class
)
@ConditionalOnClass(RepositoryRestResource.class)
@ConditionalOnWebApplication(type = Type.SERVLET)
public class AbacAutoConfiguration {

    @EnableAbac
    public static class EnableAbacAutoConfiguration {
    }
}
