package com.contentgrid.thunx.api.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

import com.contentgrid.thunx.encoding.ThunkExpressionDecoder;
import com.contentgrid.thunx.spring.data.rest.AbacExceptionHandler;
import com.contentgrid.thunx.spring.data.rest.AbacRequestFilter;
import org.junit.jupiter.api.Test;
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
            .withConfiguration(AutoConfigurations.of(
                    AbacAutoConfiguration.class
            ));

    @Test
    public void shouldEnableAbac() {

        contextRunner.withUserConfiguration(TestContext.class)
                .withInitializer(ConditionEvaluationReportLoggingListener.forLogLevel(LogLevel.INFO))
                .run((context) -> {
                    assertThat(context.getBean(QuerydslBindingsFactory.class)).isNotNull();
                    assertThat(context.getBean(ThunkExpressionDecoder.class)).isNotNull();
                    assertThat(context.getBean(AbacExceptionHandler.class)).isNotNull();
                    assertThat(context.getBean(AbacRequestFilter.class)).isNotNull();
                    assertThat(context.getBean("abacFilterRegistration")).isNotNull();
                    assertThat(context.getBean("interceptRepositoryRestMvcConfiguration")).isNotNull();
                    assertThat(context.getBean("ensureQueryDslPredication")).isNotNull();
                });
    }

    @Configuration
    @EnableAutoConfiguration
    public static class TestContext {

    }
}
