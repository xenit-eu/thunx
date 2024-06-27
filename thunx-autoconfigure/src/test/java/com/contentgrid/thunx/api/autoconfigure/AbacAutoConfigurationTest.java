package com.contentgrid.thunx.api.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

import com.contentgrid.thunx.encoding.ThunkExpressionDecoder;
import com.contentgrid.thunx.spring.data.rest.AbacExceptionHandler;
import com.contentgrid.thunx.spring.data.rest.AbacRequestFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.UnsatisfiedDependencyException;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.logging.ConditionEvaluationReportLoggingListener;
import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.querydsl.binding.QuerydslBindingsFactory;

public class AbacAutoConfigurationTest {

    WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
            .withPropertyValues("spring.cloud.gateway.enabled=false")
            .withInitializer(ConditionEvaluationReportLoggingListener.forLogLevel(LogLevel.INFO))
            .withConfiguration(AutoConfigurations.of(
                    AbacAutoConfiguration.class
            ));

    @Test
    public void shouldEnableAbacByDefault() {

        contextRunner.withUserConfiguration(TestContext.class)
                .run((context) -> {
                    assertThat(context).hasSingleBean(QuerydslBindingsFactory.class);
                    assertThat(context).hasSingleBean(ThunkExpressionDecoder.class);
                    assertThat(context).hasSingleBean(AbacExceptionHandler.class);
                    assertThat(context).hasSingleBean(AbacRequestFilter.class);
                    assertThat(context).hasBean("abacFilterRegistration");
                    assertThat(context).hasBean("interceptRepositoryRestMvcConfiguration");
                    assertThat(context).hasBean("ensureQueryDslPredication");
                    assertThat(context).hasBean("headerAbacContextSupplier");
                    assertThat(context).doesNotHaveBean("abacJwtAuthenticationConverter");
                });
    }

    @Test
    public void shouldEnableAbacWhenPropertyEqualsHeader() {

        contextRunner.withUserConfiguration(TestContext.class)
                .withSystemProperties("contentgrid.thunx.abac.source=header")
                .run((context) -> {
                    assertThat(context).hasSingleBean(QuerydslBindingsFactory.class);
                    assertThat(context).hasSingleBean(ThunkExpressionDecoder.class);
                    assertThat(context).hasSingleBean(AbacExceptionHandler.class);
                    assertThat(context).hasSingleBean(AbacRequestFilter.class);
                    assertThat(context).hasBean("abacFilterRegistration");
                    assertThat(context).hasBean("interceptRepositoryRestMvcConfiguration");
                    assertThat(context).hasBean("ensureQueryDslPredication");
                    assertThat(context).hasBean("headerAbacContextSupplier");
                    assertThat(context).doesNotHaveBean("abacJwtAuthenticationConverter");
                });
    }

    @Test
    public void shouldDisableAbacWhenPropertyEqualsNone() {

        contextRunner.withUserConfiguration(TestContext.class)
                .withSystemProperties("contentgrid.thunx.abac.source=none")
                .run((context) -> {
                    assertThat(context).hasSingleBean(QuerydslBindingsFactory.class);
                    assertThat(context).hasSingleBean(ThunkExpressionDecoder.class);
                    assertThat(context).hasSingleBean(AbacExceptionHandler.class);
                    assertThat(context).doesNotHaveBean(AbacRequestFilter.class);
                    assertThat(context).doesNotHaveBean("abacFilterRegistration");
                    assertThat(context).hasBean("interceptRepositoryRestMvcConfiguration");
                    assertThat(context).hasBean("ensureQueryDslPredication");
                    assertThat(context).hasBean("noneAbacContextSupplier");
                    assertThat(context).doesNotHaveBean("abacJwtAuthenticationConverter");
                });
    }

    @Test
    public void shouldFailWhenPropertyIsInvalid() {
        contextRunner.withUserConfiguration(TestContext.class)
                .withSystemProperties("contentgrid.thunx.abac.source=invalid")
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context).getFailure().isInstanceOf(UnsatisfiedDependencyException.class)
                            .hasMessageContaining("No qualifying bean of type 'com.contentgrid.thunx.spring.data.context.AbacContextSupplier' available");
                });
    }

    @Configuration
    @EnableAutoConfiguration
    public static class TestContext {

    }
}
