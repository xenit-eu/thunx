package com.contentgrid.thunx.api.autoconfigure;

import com.contentgrid.thunx.spring.data.EnableAbac;
import com.contentgrid.thunx.spring.data.querydsl.predicate.injector.rest.webmvc.SpringDataQuerydslPredicateInjectorAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@AutoConfiguration
@ConditionalOnClass(RepositoryRestResource.class)
@AutoConfigureBefore(SpringDataQuerydslPredicateInjectorAutoConfiguration.class)
public class AbacAutoConfiguration {

    @EnableAbac
    public static class EnableAbacAutoConfiguration {
    }
}
