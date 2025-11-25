package com.contentgrid.thunx.gateway.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.contentgrid.opa.client.OpaClient;
import com.contentgrid.thunx.api.autoconfigure.AbacAutoConfiguration;
import com.contentgrid.thunx.api.autoconfigure.AbacContextAutoConfiguration;
import com.contentgrid.thunx.api.autoconfigure.JwtAbacAutoConfiguration;
import com.contentgrid.thunx.pdp.PolicyDecisionPointClient;
import com.contentgrid.thunx.pdp.opa.OpaQueryProvider;
import com.contentgrid.thunx.spring.data.context.AbacContextSupplier;
import com.contentgrid.thunx.spring.gateway.filter.AbacGatewayFilterFactory;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.logging.ConditionEvaluationReportLoggingListener;
import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ReactiveWebApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.querydsl.binding.QuerydslBindingsFactory;
import org.springframework.security.authorization.ReactiveAuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import org.springframework.web.server.ServerWebExchange;

public class GatewayAutoConfigurationTest {

    ReactiveWebApplicationContextRunner contextRunner = new ReactiveWebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    GatewayAutoConfiguration.class
            ));

    @Test
    void conditionalOnPropertyOpaServiceUrl() {

        contextRunner.withUserConfiguration(TestContext.class)
                .run((context) -> {
                    assertThat(context).hasSingleBean(OpaProperties.class);
                    assertThat(context).hasSingleBean(OpaQueryProvider.class);

                    assertThat(context).doesNotHaveBean(OpaClient.class);
                    assertThat(context).doesNotHaveBean(PolicyDecisionPointClient.class);
                    assertThat(context).doesNotHaveBean(ReactiveAuthorizationManager.class);
                });
    }

    @Test
    void shouldEnableGatewayBeans() {

        var OPA_SERVICE_URL = "https://some/opa/service";
        contextRunner.withUserConfiguration(TestContext.class)
            .withInitializer(ConditionEvaluationReportLoggingListener.forLogLevel(LogLevel.INFO))
            .withPropertyValues("opa.service.url="+OPA_SERVICE_URL)
            .run((context) -> {
                assertThat(context).hasSingleBean(OpaProperties.class)
                        .getBean(OpaProperties.class)
                        .satisfies(opa -> assertThat(opa.getService().getUrl()).isEqualTo(OPA_SERVICE_URL));

                assertThat(context).hasSingleBean(OpaClient.class);
                assertThat(context).hasSingleBean(OpaQueryProvider.class);
                assertThat(context).hasSingleBean(PolicyDecisionPointClient.class);
                assertThat(context).hasSingleBean(ReactiveAuthorizationManager.class);
                assertThat(context).hasSingleBean(AbacGatewayFilterFactory.class);
        });
    }

    @Test
    void shouldUseProvidedGatewayBeans() {

        contextRunner.withUserConfiguration(TestContextWithBeans.class)
                .run((context) -> {
                    assertThat(context).getBean(OpaClient.class).isSameAs(context.getBean(TestContextWithBeans.class).opaClient());
                    assertThat(context).getBean(OpaQueryProvider.class).isSameAs(context.getBean(TestContextWithBeans.class).customQueryProvider());
                    assertThat(context).getBean(PolicyDecisionPointClient.class).isSameAs(context.getBean(TestContextWithBeans.class).pdpClient());
                    assertThat(context).getBean(ReactiveAuthorizationManager.class).isSameAs(context.getBean(TestContextWithBeans.class).reactiveAuthenticationManager());
                    assertThat(context).getBean(AbacGatewayFilterFactory.class).isSameAs(context.getBean(TestContextWithBeans.class).abacGatewayFilterFactory());
                });
    }

    @Test
    void shouldNotFailWithApiAutoconfiguations() {
        contextRunner.withUserConfiguration(TestContext.class)
                .withConfiguration(AutoConfigurations.of(AbacContextAutoConfiguration.class, AbacAutoConfiguration.class, JwtAbacAutoConfiguration.class))
                .withClassLoader(new FilteredClassLoader(
                        "com.contentgrid.thunx.spring.data",
                        "com.contentgrid.thunx.predicates.querydsl"
                ))
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).hasSingleBean(OpaProperties.class);

                    assertThat(context).doesNotHaveBean(QuerydslBindingsFactory.class);
                    assertThat(context).doesNotHaveBean(AbacContextSupplier.class);
                });
    }

    @Configuration
    @EnableAutoConfiguration
    public static class TestContext {
    }

    @Configuration
    @EnableAutoConfiguration
    public static class TestContextWithBeans {

        @Bean
        public OpaClient opaClient() {
            return mock(OpaClient.class);
        }

        @Bean
        public PolicyDecisionPointClient<Authentication, ServerWebExchange> pdpClient() {
            return mock(PolicyDecisionPointClient.class);
        }

        @Bean
        public ReactiveAuthorizationManager<AuthorizationContext> reactiveAuthenticationManager() {
            return mock(ReactiveAuthorizationManager.class);
        }

        @Bean
        public AbacGatewayFilterFactory abacGatewayFilterFactory() {
            return mock(AbacGatewayFilterFactory.class);
        }

        @Bean
        public OpaQueryProvider<ServerWebExchange> customQueryProvider() {
            return mock(OpaQueryProvider.class);
        }
    }
}
