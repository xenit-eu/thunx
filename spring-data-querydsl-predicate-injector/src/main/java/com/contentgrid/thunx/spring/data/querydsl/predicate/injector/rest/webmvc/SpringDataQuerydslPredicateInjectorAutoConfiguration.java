package com.contentgrid.thunx.spring.data.querydsl.predicate.injector.rest.webmvc;

import com.contentgrid.thunx.spring.data.querydsl.predicate.injector.repository.RepositoryInvokerAdapterFactory;
import com.contentgrid.thunx.spring.data.querydsl.predicate.injector.resolver.QuerydslPredicateResolver;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.data.rest.autoconfigure.DataRestAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.querydsl.binding.QuerydslBindingsFactory;
import org.springframework.data.repository.support.Repositories;
import org.springframework.data.repository.support.RepositoryInvokerFactory;
import org.springframework.data.rest.webmvc.config.RepositoryRestMvcConfiguration;
import org.springframework.data.rest.webmvc.config.ResourceMetadataHandlerMethodArgumentResolver;
import org.springframework.data.rest.webmvc.config.RootResourceInformationHandlerMethodArgumentResolver;

@AutoConfiguration
@AutoConfigureAfter(DataRestAutoConfiguration.class)
@ConditionalOnClass(RepositoryRestMvcConfiguration.class)
@ConditionalOnBean(RepositoryRestMvcConfiguration.class)
public class SpringDataQuerydslPredicateInjectorAutoConfiguration {

    @Bean
    BeanPostProcessor interceptRepositoryRestMvcConfiguration(ApplicationContext applicationContext) {
        return new BeanPostProcessor() {
            @Override
            public @Nullable Object postProcessAfterInitialization(@Nullable Object bean, @Nullable String beanName) throws BeansException {

                if (bean instanceof RootResourceInformationHandlerMethodArgumentResolver) {
                    var repositories = applicationContext.getBean(Repositories.class);
                    var invokerFactory = applicationContext.getBean(RepositoryInvokerFactory.class);
                    var resourceMetadataResolver = applicationContext.getBean(
                            ResourceMetadataHandlerMethodArgumentResolver.class);

                    var predicateResolvers = applicationContext.getBeanProvider(QuerydslPredicateResolver.class);
                    var repositoryInvokerAdapterFactory = applicationContext.getBean(
                            RepositoryInvokerAdapterFactory.class);

                    return new PredicateInjectingRootResourceInformationHandlerMethodArgumentResolver(
                            repositories,
                            invokerFactory,
                            resourceMetadataResolver,
                            repositoryInvokerAdapterFactory,
                            predicateResolvers
                    );
                }

                return bean;
            }
        };
    }

    @Bean
    @ConditionalOnMissingBean
    RepositoryInvokerAdapterFactory repositoryInvokerAdapterFactory(Repositories repositories) {
        return new QuerydslRepositoryInvokerAdapterFactory(repositories);
    }

    @Bean
    @ConditionalOnBean(QuerydslBindingsFactory.class)
    QuerydslPredicateResolver querydslBindingsPredicateResolver(
            @Qualifier("defaultConversionService") ConversionService conversionService,
            QuerydslBindingsFactory querydslBindingsFactory
    ) {
        return new QuerydslBindingsPredicateResolver(conversionService, querydslBindingsFactory);
    }
}