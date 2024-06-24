package com.contentgrid.thunx.spring.data.rest;

import com.contentgrid.thunx.encoding.ThunkExpressionDecoder;
import com.contentgrid.thunx.encoding.json.JsonThunkExpressionCoder;
import com.contentgrid.thunx.spring.data.querydsl.AbacQuerydslPredicateResolver;
import com.contentgrid.thunx.spring.data.querydsl.predicate.injector.repository.RepositoryInvokerAdapterFactory;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBindingsFactory;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.support.Repositories;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class AbacConfiguration {

    @Bean
    public ThunkExpressionDecoder thunkDecoder() {
        return new JsonThunkExpressionCoder();
    }

    @Bean
    public AbacExceptionHandler exceptionHandler() {
        return new AbacExceptionHandler();
    }

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

    @Bean
    public ApplicationListener<ContextRefreshedEvent> ensureAbacQueryDslResolverExist() {
        return new ApplicationListener<ContextRefreshedEvent>() {
            @Override
            public void onApplicationEvent(ContextRefreshedEvent event) {
                if (event.getApplicationContext().getBeanNamesForType(AbacQuerydslPredicateResolver.class).length == 0) {
                    throw new IllegalArgumentException("Property 'contentgrid.thunx.abac.source' contains an unknown"
                            + " value, supported values are 'header', 'jwt' or 'none'.");
                }
            }
        };
    }
}
