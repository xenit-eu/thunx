package com.contentgrid.thunx.opa.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.contentgrid.opa.client.OpaClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@ExtendWith(OutputCaptureExtension.class)
class OpaClientAutoConfigurationTest {

    ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(OpaClientAutoConfiguration.class));

    @Test
    void warnsWhenServiceUrlIsMissing(CapturedOutput output) {
        contextRunner.run((context) -> {
            assertThat(context).hasSingleBean(OpaProperties.class);
            assertThat(context).doesNotHaveBean(OpaClient.class);

            assertThat(output).contains("opa.service.url");
        });
    }

    @Test
    void doesNotWarnWhenServiceUrlIsConfigured(CapturedOutput output) {
        contextRunner
                .withPropertyValues("opa.service.url=https://some/opa/service")
                .run((context) -> {
                    assertThat(context).hasSingleBean(OpaClient.class);

                    assertThat(output).doesNotContain("opa.service.url' is not configured");
                });
    }

    @Test
    void doesNotWarnWhenCustomOpaClientIsConfigured(CapturedOutput output) {
        contextRunner
                .withBean(OpaClient.class, () -> mock(OpaClient.class))
                .run((context) -> {
                    assertThat(context).hasSingleBean(OpaClient.class);

                    assertThat(output).doesNotContain("opa.service.url' is not configured");
                });
    }
}
