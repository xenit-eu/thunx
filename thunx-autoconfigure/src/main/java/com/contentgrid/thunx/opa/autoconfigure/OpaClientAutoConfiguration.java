package com.contentgrid.thunx.opa.autoconfigure;

import com.contentgrid.opa.client.OpaClient;
import com.contentgrid.opa.client.rest.RestClientConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StringUtils;

@Slf4j
@AutoConfiguration
@ConditionalOnClass(OpaClient.class)
@EnableConfigurationProperties(OpaProperties.class)
public class OpaClientAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty("opa.service.url")
    public OpaClient opaClient(OpaProperties opaProperties) {
        return OpaClient.builder()
                .httpLogging(RestClientConfiguration.LogSpecification::all)
                .url(opaProperties.getService().getUrl())
                .build();
    }

    // Runs after all singletons (including any user-supplied OpaClient bean) have been instantiated,
    // so it only warns when the context genuinely ends up without an OpaClient bean.
    @Bean
    SmartInitializingSingleton opaClientMissingWarning(OpaProperties opaProperties, ObjectProvider<OpaClient> opaClient) {
        return () -> {
            if (opaClient.getIfAvailable() == null && !StringUtils.hasText(opaProperties.getService().getUrl())) {
                log.warn("OPA client is on the classpath, but 'opa.service.url' is not configured; no OpaClient bean will be created");
            }
        };
    }
}
