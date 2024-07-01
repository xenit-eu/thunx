package com.contentgrid.thunx.gateway.autoconfigure;

import com.contentgrid.opa.client.OpaClient;
import com.contentgrid.opa.client.rest.OpaHttpClient;
import com.contentgrid.opa.client.rest.RestClientConfiguration;
import com.contentgrid.opa.client.rest.client.jdk.DefaultOpaHttpClient;
import com.contentgrid.thunx.pdp.PolicyDecisionComponentImpl;
import com.contentgrid.thunx.pdp.PolicyDecisionPointClient;
import com.contentgrid.thunx.pdp.opa.OpaInputProvider;
import com.contentgrid.thunx.pdp.opa.OpaQueryProvider;
import com.contentgrid.thunx.pdp.opa.OpenPolicyAgentPDPClient;
import com.contentgrid.thunx.spring.gateway.filter.AbacGatewayFilterFactory;
import com.contentgrid.thunx.spring.security.DefaultOpaInputProvider;
import com.contentgrid.thunx.spring.security.ReactivePolicyAuthorizationManager;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.time.Duration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authorization.ReactiveAuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import org.springframework.web.server.ServerWebExchange;

@AutoConfiguration(after = OpaHttpClientInstrumentationAutoConfiguration.class)
@ConditionalOnClass({OpaClient.class, AbstractGatewayFilterFactory.class})
@EnableConfigurationProperties(OpaProperties.class)
@ConditionalOnWebApplication(type = Type.REACTIVE)
public class GatewayAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public OpaHttpClient opaHttpClient() {
        return new DefaultOpaHttpClient(
                HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(5))
                        .followRedirects(Redirect.NORMAL)
                        .build(),
                JsonMapper.builder()
                        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                        .build()
                        .registerModule(new JavaTimeModule()));
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty("opa.service.url")
    public OpaClient opaClient(OpaProperties opaProperties, OpaHttpClient opaHttpClient) {
        return OpaClient.builder()
                .restClient(opaHttpClient)
                .httpLogging(RestClientConfiguration.LogSpecification::all)
                .url(opaProperties.getService().getUrl())
                .build();
    }

    @Bean
    @ConditionalOnMissingBean
    public OpaQueryProvider<ServerWebExchange> propertyBasedOpaQueryProvider(OpaProperties opaProperties) {
        return request -> opaProperties.getQuery();
    }

    @Bean
    @ConditionalOnMissingBean
    public OpaInputProvider<Authentication, ServerWebExchange> defaultOpaInputProvider() {
        return new DefaultOpaInputProvider();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(OpaClient.class)
    public PolicyDecisionPointClient<Authentication, ServerWebExchange> pdpClient(OpaClient opaClient, OpaQueryProvider<ServerWebExchange> queryProvider, OpaInputProvider<Authentication, ServerWebExchange> inputProvider) {
        return new OpenPolicyAgentPDPClient<>(opaClient, queryProvider, inputProvider);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(PolicyDecisionPointClient.class)
    public ReactiveAuthorizationManager<AuthorizationContext> reactiveAuthenticationManager(
            PolicyDecisionPointClient<Authentication, ServerWebExchange> pdpClient) {
        return new ReactivePolicyAuthorizationManager(new PolicyDecisionComponentImpl<>(pdpClient));
    }

    @Bean
    @ConditionalOnMissingBean
    public AbacGatewayFilterFactory abacGatewayFilterFactory() {
        return new AbacGatewayFilterFactory();
    }
}
