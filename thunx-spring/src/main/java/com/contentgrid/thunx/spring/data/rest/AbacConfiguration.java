package com.contentgrid.thunx.spring.data.rest;

import com.contentgrid.thunx.encoding.ThunkExpressionDecoder;
import com.contentgrid.thunx.encoding.json.ExpressionJsonConverter;
import com.contentgrid.thunx.predicates.model.ThunkExpression;
import com.contentgrid.thunx.spring.data.querydsl.DefaultQuerydslPredicateResolver;
import com.contentgrid.thunx.spring.data.querydsl.QuerydslPredicateResolver;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import com.contentgrid.thunx.spring.data.querydsl.AbacQuerydslPredicateResolver;
import org.springframework.data.querydsl.binding.QuerydslBindingsFactory;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.support.Repositories;
import org.springframework.data.repository.support.RepositoryInvokerFactory;
import org.springframework.data.rest.webmvc.config.ResourceMetadataHandlerMethodArgumentResolver;
import org.springframework.data.rest.webmvc.config.RootResourceInformationHandlerMethodArgumentResolver;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class AbacConfiguration {

    @Bean
    public ThunkExpressionDecoder thunkDecoder() {
        return data -> {
            try {
                var json = new String(data, StandardCharsets.UTF_8);
                var expression = new ExpressionJsonConverter().decode(json);

                return ((ThunkExpression<Boolean>) expression);
            } catch (JsonProcessingException e) {
                throw new UncheckedIOException(e);
            }
        };
    }

    @Bean
    public AbacExceptionHandler exceptionHandler() {
        return new AbacExceptionHandler();
    }

    @Bean
    public AbacRequestFilter abacFilter(ThunkExpressionDecoder thunkDecoder) {
        return new AbacRequestFilter(thunkDecoder);
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
    public BeanPostProcessor interceptRepositoryRestMvcConfiguration(ApplicationContext applicationContext)
    {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {


                if (bean instanceof RootResourceInformationHandlerMethodArgumentResolver) {
                    var repositories = applicationContext.getBean(Repositories.class);
                    var invokerFactory = applicationContext.getBean(RepositoryInvokerFactory.class);
                    var resourceMetadataResolver = applicationContext.getBean(ResourceMetadataHandlerMethodArgumentResolver.class);
                    var factory = applicationContext.getBean(QuerydslBindingsFactory.class);
                    var transactionManager = applicationContext.getBean(PlatformTransactionManager.class);
                    var entityPathResolver = factory.getEntityPathResolver();

                    var defaultConversionService = applicationContext.getBean("defaultConversionService", ConversionService.class);
                    var predicateResolvers = applicationContext.getBeanProvider(QuerydslPredicateResolver.class);
                    var repositoryInvokerFactory = new AbacRepositoryInvokerAdapterFactory(
                            repositories,
                            transactionManager,
                            entityPathResolver,
                            defaultConversionService
                    );


                    return new AbacRootResourceInformationHandlerMethodArgumentResolver(
                            repositories,
                            invokerFactory,
                            resourceMetadataResolver,
                            predicateResolvers,
                            repositoryInvokerFactory
                    );
                }

                return bean;
            }
        };
    }

    @Bean
    QuerydslPredicateResolver defaultQuerydslPredicateResolver(
            @Qualifier("defaultConversionService") ConversionService conversionService,
            QuerydslBindingsFactory querydslBindingsFactory
    ) {
        return new DefaultQuerydslPredicateResolver(conversionService, querydslBindingsFactory);
    }

    @Bean
    QuerydslPredicateResolver abacQuerydslPredicateResolver(QuerydslBindingsFactory querydslBindingsFactory) {
        return new AbacQuerydslPredicateResolver(querydslBindingsFactory.getEntityPathResolver());
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
