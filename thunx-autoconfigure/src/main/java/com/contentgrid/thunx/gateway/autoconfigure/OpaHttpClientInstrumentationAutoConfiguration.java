package com.contentgrid.thunx.gateway.autoconfigure;

import com.contentgrid.opa.client.rest.OpaHttpClient;
import com.contentgrid.opa.client.rest.client.jdk.DefaultOpaHttpClient;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.java11.instrument.binder.jdk.MicrometerHttpClient;
import io.micrometer.observation.ObservationRegistry;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.time.Duration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass({MicrometerHttpClient.class, ObservationRegistry.class, MeterRegistry.class})
@ConditionalOnBean({ObservationRegistry.class, MeterRegistry.class})
public class OpaHttpClientInstrumentationAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public OpaHttpClient opaHttpClient(ObservationRegistry observationRegistry, MeterRegistry meterRegistry) {
        var httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .followRedirects(Redirect.NORMAL)
                .build();

        var observedClient = MicrometerHttpClient.instrumentationBuilder(httpClient, meterRegistry)
                .observationRegistry(observationRegistry)
                .build();

        return new DefaultOpaHttpClient(observedClient,
                JsonMapper.builder()
                        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                        .build()
                        .registerModule(new JavaTimeModule()));
    }

}
