package com.contentgrid.thunx.webmvc.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.contentgrid.opa.client.OpaClient;
import com.contentgrid.thunx.opa.autoconfigure.OpaClientAutoConfiguration;
import com.contentgrid.thunx.pdp.PolicyDecisionPointClient;
import com.contentgrid.thunx.spring.webmvc.PolicyAuthorizationManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.UnsatisfiedDependencyException;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.logging.ConditionEvaluationReportLoggingListener;
import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authorization.AuthorizationManager;

class WebMvcAbacAutoConfigurationTest {

    WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
            .withInitializer(ConditionEvaluationReportLoggingListener.forLogLevel(LogLevel.INFO))
            .withConfiguration(AutoConfigurations.of(
                    OpaClientAutoConfiguration.class,
                    WebMvcAbacAutoConfiguration.class
            ));

    @Test
    void backsOffWhenSourceIsNotOpa() {
        contextRunner.run((context) -> {
            assertThat(context).doesNotHaveBean(WebMvcAbacAutoConfiguration.class);
            assertThat(context).doesNotHaveBean(PolicyAuthorizationManager.class);
        });
    }

    @Test
    void failsFastWithoutOpaServiceUrl() {
        contextRunner
                .withPropertyValues("contentgrid.thunx.abac.source=opa")
                .run((context) -> {
                    assertThat(context).hasFailed();
                    assertThat(context).getFailure().isInstanceOf(UnsatisfiedDependencyException.class)
                            .hasMessageContaining("com.contentgrid.opa.client.OpaClient");
                });
    }

    @Test
    void shouldEnableOpaAbacInServletApp() {
        var opaServiceUrl = "https://some/opa/service";
        contextRunner
                .withPropertyValues(
                        "contentgrid.thunx.abac.source=opa",
                        "opa.service.url=" + opaServiceUrl)
                .run((context) -> {
                    assertThat(context).hasSingleBean(OpaClient.class);
                    assertThat(context).hasSingleBean(PolicyDecisionPointClient.class);
                    assertThat(context).getBean(AuthorizationManager.class)
                            .isInstanceOf(PolicyAuthorizationManager.class);
                });
    }

    @Test
    void shouldEnableOpaAbacWithCustomOpaClientAndNoServiceUrl() {
        contextRunner
                .withBean(OpaClient.class, () -> mock(OpaClient.class))
                .withPropertyValues("contentgrid.thunx.abac.source=opa")
                .run((context) -> {
                    assertThat(context).hasSingleBean(OpaClient.class);
                    assertThat(context).hasSingleBean(PolicyDecisionPointClient.class);
                    assertThat(context).getBean(AuthorizationManager.class)
                            .isInstanceOf(PolicyAuthorizationManager.class);
                });
    }
}
