package com.contentgrid.thunx.api.autoconfigure;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import com.contentgrid.thunx.encoding.ThunkExpressionDecoder;
import com.contentgrid.thunx.gateway.autoconfigure.GatewayAutoConfiguration;
import com.contentgrid.thunx.spring.data.rest.AbacExceptionHandler;
import com.contentgrid.thunx.spring.data.rest.AbacRequestFilter;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

public class AbacAutoConfigurationTest {

    @Test
    public void shouldEnableAbac() {

        WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
                .withPropertyValues("spring.cloud.gateway.enabled=false")
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
    }
}
