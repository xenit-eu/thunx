package com.contentgrid.thunx.api.autoconfigure;

import com.contentgrid.thunx.spring.data.querydsl.predicate.injector.rest.webmvc.SpringDataQuerydslPredicateInjectorAutoConfiguration;
import com.contentgrid.thunx.spring.data.rest.AbacRestConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.boot.data.rest.autoconfigure.DataRestAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@AutoConfiguration(
        before = SpringDataQuerydslPredicateInjectorAutoConfiguration.class,
        after = DataRestAutoConfiguration.class
)
@ConditionalOnClass({RepositoryRestResource.class, SpringDataQuerydslPredicateInjectorAutoConfiguration.class})
@ConditionalOnWebApplication(type = Type.SERVLET)
@Import(AbacRestConfiguration.class)
public class AbacAutoConfiguration {
}
