package com.contentgrid.thunx.spring.data.rest;

import com.contentgrid.thunx.spring.data.context.AbacContextSupplier;
import com.contentgrid.thunx.spring.data.querydsl.AbacQuerydslPredicateResolver;
import com.contentgrid.thunx.spring.data.querydsl.predicate.injector.repository.RepositoryInvokerAdapterFactory;
import com.contentgrid.thunx.spring.data.querydsl.predicate.injector.resolver.QuerydslPredicateResolver;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBindingsFactory;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.support.Repositories;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class AbacRestConfiguration {

    @Bean
    RepositoryInvokerAdapterFactory abacRepositoryInvokerAdapterFactory(
            Repositories repositories,
            PlatformTransactionManager transactionManager,
            QuerydslBindingsFactory querydslBindingsFactory,
            @Qualifier("defaultConversionService") ConversionService conversionService
    ) {
        return new AbacRepositoryInvokerAdapterFactory(
                repositories,
                transactionManager,
                querydslBindingsFactory.getEntityPathResolver(),
                conversionService
        );
    }

    @Bean
    public QuerydslPredicateResolver abacQuerydslPredicateResolver(QuerydslBindingsFactory querydslBindingsFactory,
            AbacContextSupplier abacContextSupplier) {
        return new AbacQuerydslPredicateResolver(querydslBindingsFactory.getEntityPathResolver(), abacContextSupplier);
    }

    @Bean
    public BeanPostProcessor ensureQueryDslPredication(ApplicationContext applicationContext) {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {

                if (bean instanceof Repository) {
                    if (bean instanceof QuerydslPredicateExecutor == false) {

                        if (AopUtils.isJdkDynamicProxy(bean)) {
                            Class<?>[] proxiedInterfaces = ((Advised)bean).getProxiedInterfaces();
                            if (proxiedInterfaces != null && proxiedInterfaces.length >= 1) {
                                throw new IllegalStateException(String.format("%s must implement QueryDslPredicateExecutor when using AbacConfiguration", proxiedInterfaces[0]));
                            }
                        }
                        throw new IllegalStateException("All repositories must implement QueryDslPredicateExecutor when using AbacConfiguration");
                    }
                }

                return bean;
            }
        };
    }
}
