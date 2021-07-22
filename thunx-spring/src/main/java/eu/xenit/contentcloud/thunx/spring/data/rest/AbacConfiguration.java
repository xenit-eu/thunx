package eu.xenit.contentcloud.thunx.spring.data.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import eu.xenit.contentcloud.thunx.encoding.ThunkExpressionDecoder;
import eu.xenit.contentcloud.thunx.predicates.model.ThunkExpression;
import eu.xenit.contentcloud.thunx.encoding.json.ExpressionJsonConverter;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import javax.persistence.EntityManager;

import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.AbacQuerydslPredicateBuilder;
import org.springframework.data.querydsl.binding.QuerydslBindingsFactory;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.support.Repositories;
import org.springframework.data.repository.support.RepositoryInvokerFactory;
import org.springframework.data.rest.webmvc.config.ResourceMetadataHandlerMethodArgumentResolver;
import org.springframework.data.rest.webmvc.config.RootResourceInformationHandlerMethodArgumentResolver;
import org.springframework.format.support.DefaultFormattingConversionService;
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
    public AbacRequestFilter abacFilter(ThunkExpressionDecoder thunkDecoder, Repositories repos, EntityManager em, PlatformTransactionManager tm) {
        return new AbacRequestFilter(thunkDecoder, repos, em, tm);
    }

    @Bean
    public FilterRegistrationBean<AbacRequestFilter> abacFilterRegistration(AbacRequestFilter filter, Repositories repos, EntityManager em, PlatformTransactionManager tm) {
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

                    var defaultConversionService = new DefaultFormattingConversionService(); // ??
                    var predicateBuilder = new AbacQuerydslPredicateBuilder(defaultConversionService, factory.getEntityPathResolver());


                    return new AbacRootResourceInformationHandlerMethodArgumentResolver(
                            repositories,
                            invokerFactory,
                            resourceMetadataResolver,
                            predicateBuilder,
                            factory
                    );
                }

                return bean;
            }
        };
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
