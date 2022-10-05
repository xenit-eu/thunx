package com.contentgrid.thunx.api.autoconfigure;

import com.contentgrid.thunx.gateway.autoconfigure.GatewayAutoConfiguration;
import com.contentgrid.thunx.encoding.ThunkExpressionDecoder;
import com.contentgrid.thunx.spring.data.rest.AbacExceptionHandler;
import com.contentgrid.thunx.spring.data.rest.AbacRequestFilter;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.mapping.ResourceMappings;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

public class AbacAutoConfigurationTest {

    @Test
    public void shouldEnableAbac() {

        ApplicationContextRunner contextRunner = new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(
                        AbacAutoConfiguration.class
                ));

        contextRunner.withUserConfiguration(TestContext.class).run((context) -> {
            assertThat(context.getBean(ThunkExpressionDecoder.class), is(not(nullValue())));
            assertThat(context.getBean(AbacExceptionHandler.class), is(not(nullValue())));
            assertThat(context.getBean(AbacRequestFilter.class), is(not(nullValue())));
            assertThat(context.getBean("abacFilterRegistration"), is(not(nullValue())));
            assertThat(context.getBean("interceptRepositoryRestMvcConfiguration"), is(not(nullValue())));
            assertThat(context.getBean("ensureQueryDslPredication"), is(not(nullValue())));
        });
    }

    @Configuration
    @EnableAutoConfiguration(exclude={
            org.springframework.cloud.gateway.config.GatewayAutoConfiguration.class,
            GatewayAutoConfiguration.class
    })
    public static class TestContext {
        @Bean
        public ResourceMappings resourceMappings() {
            return Mockito.mock(ResourceMappings.class);
        }
    }
}
