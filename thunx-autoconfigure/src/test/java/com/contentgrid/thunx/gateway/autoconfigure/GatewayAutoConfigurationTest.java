package com.contentgrid.thunx.gateway.autoconfigure;

import com.contentgrid.opa.client.OpaClient;
import com.contentgrid.thunx.api.autoconfigure.AbacAutoConfiguration;
import com.contentgrid.thunx.pdp.AuthenticationContext;
import com.contentgrid.thunx.pdp.PolicyDecisionPointClient;
import com.contentgrid.thunx.pdp.RequestContext;
import com.contentgrid.thunx.pdp.opa.OpaQueryProvider;
import com.contentgrid.thunx.spring.gateway.filter.AbacGatewayFilterFactory;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authorization.ReactiveAuthorizationManager;
import org.springframework.security.web.server.authorization.AuthorizationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class GatewayAutoConfigurationTest {

    @Test
    public void shouldEnableGatewayBeans() {

        ApplicationContextRunner contextRunner = new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(
                        GatewayAutoConfiguration.class
                ));

        contextRunner.withUserConfiguration(TestContext.class)
            .withPropertyValues("opa.service.url=https://some/opa/service")
            .run((context) -> {
                assertThat(context.getBean(OpaClient.class)).isNotNull();
                assertThat(context.getBean(OpaQueryProvider.class)).isNotNull();
                assertThat(context.getBean(PolicyDecisionPointClient.class)).isNotNull();
                assertThat(context.getBean(ReactiveAuthorizationManager.class)).isNotNull();
                assertThat(context.getBean(AbacGatewayFilterFactory.class)).isNotNull();
        });
    }

    @Test
    public void shouldUseProvidedGatewayBeans() {

        ApplicationContextRunner contextRunner = new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(
                        GatewayAutoConfiguration.class
                ));

        contextRunner.withUserConfiguration(TestContextWithBeans.class)
                .run((context) -> {
                    assertThat(context.getBean(OpaClient.class)).isSameAs(context.getBean(TestContextWithBeans.class).opaClient());
                    assertThat(context.getBean(OpaQueryProvider.class)).isSameAs(context.getBean(TestContextWithBeans.class).customQueryProvider());
                    assertThat(context.getBean(PolicyDecisionPointClient.class)).isSameAs(context.getBean(TestContextWithBeans.class).pdpClient());
                    assertThat(context.getBean(ReactiveAuthorizationManager.class)).isSameAs(context.getBean(TestContextWithBeans.class).reactiveAuthenticationManager());
                    assertThat(context.getBean(AbacGatewayFilterFactory.class)).isSameAs(context.getBean(TestContextWithBeans.class).abacGatewayFilterFactory());
                });
    }

    @Configuration
    @EnableAutoConfiguration(exclude={
            org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration.class,
            org.springframework.cloud.gateway.config.GatewayAutoConfiguration.class,
            AbacAutoConfiguration.class
    })
    public static class TestContext {
    }

    @Configuration
    @EnableAutoConfiguration(exclude={
            org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration.class,
            org.springframework.cloud.gateway.config.GatewayAutoConfiguration.class,
            AbacAutoConfiguration.class
    })
    public static class TestContextWithBeans {

        @Bean
        public OpaClient opaClient() {
            return mock(OpaClient.class);
        }

        @Bean
        @ConditionalOnMissingBean
        public PolicyDecisionPointClient<AuthenticationContext, RequestContext> pdpClient() {
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
        public OpaQueryProvider<RequestContext> customQueryProvider() {
            return mock(OpaQueryProvider.class);
        }
    }
}
