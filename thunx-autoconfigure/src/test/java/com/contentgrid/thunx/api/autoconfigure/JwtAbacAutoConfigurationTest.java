package com.contentgrid.thunx.api.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

import com.contentgrid.thunx.encoding.ThunkExpressionDecoder;
import com.contentgrid.thunx.spring.security.JwtAbacConfiguration;
import com.contentgrid.thunx.spring.data.rest.AbacExceptionHandler;
import com.contentgrid.thunx.spring.data.rest.AbacRequestFilter;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.logging.ConditionEvaluationReportLoggingListener;
import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.querydsl.binding.QuerydslBindingsFactory;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

class JwtAbacAutoConfigurationTest {

    WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
            .withPropertyValues("spring.cloud.gateway.enabled=false")
            .withInitializer(ConditionEvaluationReportLoggingListener.forLogLevel(LogLevel.INFO))
            .withConfiguration(AutoConfigurations.of(
                    AbacAutoConfiguration.class, JwtAbacAutoConfiguration.class
            ));

    @Test
    public void shouldEnableAbacWhenPropertyEqualsJwt() {

        contextRunner.withUserConfiguration(TestContext.class)
                .withSystemProperties("contentgrid.thunx.abac.source=jwt")
                .run((context) -> {
                    assertThat(context).hasSingleBean(QuerydslBindingsFactory.class);
                    assertThat(context).hasSingleBean(ThunkExpressionDecoder.class);
                    assertThat(context).hasSingleBean(AbacExceptionHandler.class);
                    assertThat(context).doesNotHaveBean(AbacRequestFilter.class);
                    assertThat(context).doesNotHaveBean("abacFilterRegistration");
                    assertThat(context).hasBean("interceptRepositoryRestMvcConfiguration");
                    assertThat(context).hasBean("ensureQueryDslPredication");
                    assertThat(context).hasBean("ensureAbacQueryDslResolverExist");
                    assertThat(context).getBean("abacJwtAuthenticationConverter", JwtAuthenticationConverter.class).isNotNull();
                });
    }

    @Test
    public void shouldFailWithMissingJwtAbacConfiguration() {
        contextRunner.withUserConfiguration(TestContext.class)
                .withClassLoader(new FilteredClassLoader(JwtAbacConfiguration.class))
                .withSystemProperties("contentgrid.thunx.abac.source=jwt")
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context).getFailure().isInstanceOf(IllegalArgumentException.class)
                            .hasMessageContaining("Property 'contentgrid.thunx.abac.source' contains an unknown value");
                });
    }

    @Test
    public void shouldFailWithMissingSpringSecurity() {
        contextRunner.withUserConfiguration(TestContext.class)
                .withClassLoader(new FilteredClassLoader(Jwt.class))
                .withSystemProperties("contentgrid.thunx.abac.source=jwt")
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context).getFailure().isInstanceOf(IllegalArgumentException.class)
                            .hasMessageContaining("Property 'contentgrid.thunx.abac.source' contains an unknown value");
                });
    }

    @Configuration
    @EnableAutoConfiguration
    public static class TestContext {

    }
}