package com.contentgrid.thunx.spring.data.rest;

import com.contentgrid.thunx.encoding.ThunkExpressionDecoder;
import com.contentgrid.thunx.encoding.json.JsonThunkExpressionCoder;
import com.contentgrid.thunx.spring.data.querydsl.AbacQuerydslPredicateResolver;
import com.contentgrid.thunx.spring.data.querydsl.predicate.injector.repository.RepositoryInvokerAdapterFactory;
import com.contentgrid.thunx.spring.data.querydsl.predicate.injector.resolver.QuerydslPredicateResolver;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
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
    public AbacRequestFilter abacFilter(ThunkExpressionDecoder thunkDecoder,
            @Value("${contentgrid.thunx.allow-missing-abac:false}") boolean allowMissingAbac) {
        return new AbacRequestFilter(thunkDecoder, allowMissingAbac);
    }

    @Bean
    public FilterRegistrationBean<AbacRequestFilter> abacFilterRegistration(AbacRequestFilter filter) {
        FilterRegistrationBean<AbacRequestFilter> registrationBean = new FilterRegistrationBean<>();

        registrationBean.setFilter(filter);
//        registrationBean.addUrlPatterns("/accountStates/*");
//        registrationBean.addUrlPatterns("/content/*");

        return registrationBean;
    }

    @Bean
    QuerydslPredicateResolver abacQuerydslPredicateResolver(QuerydslBindingsFactory querydslBindingsFactory) {
        return new AbacQuerydslPredicateResolver(querydslBindingsFactory.getEntityPathResolver());
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
                                throw new IllegalStateException(String.format("%s must implement QueryDslPredicateExecutor when using @EnableAbac", proxiedInterfaces[0]));
                            }
                        }
                        throw new IllegalStateException("All repositories must implement QueryDslPredicateExecutor when using @EnableAbac");
                    }
                }

                return bean;
            }
        };
    }
}
