package com.contentgrid.thunx.gateway.autoconfigure;

import com.contentgrid.opa.client.OpaClient;
import com.contentgrid.opa.client.rest.RestClientConfiguration;
import com.contentgrid.thunx.pdp.PolicyDecisionComponentImpl;
import com.contentgrid.thunx.pdp.PolicyDecisionPointClient;
import com.contentgrid.thunx.pdp.opa.OpaInputProvider;
import com.contentgrid.thunx.pdp.opa.OpaQueryProvider;
import com.contentgrid.thunx.pdp.opa.OpenPolicyAgentPDPClient;
import com.contentgrid.thunx.spring.gateway.filter.AbacGatewayFilterFactory;
import com.contentgrid.thunx.spring.security.DefaultOpaInputProvider;
import com.contentgrid.thunx.spring.security.ReactivePolicyAuthorizationManager;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authorization.ReactiveAuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import org.springframework.web.server.ServerWebExchange;

@AutoConfiguration
@ConditionalOnClass({OpaClient.class, AbstractGatewayFilterFactory.class})
@EnableConfigurationProperties(OpaProperties.class)
public class GatewayAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public OpaClient opaClient(OpaProperties opaProperties) {
        return OpaClient.builder()
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
    public PolicyDecisionPointClient<Authentication, ServerWebExchange> pdpClient(OpaClient opaClient, OpaQueryProvider<ServerWebExchange> queryProvider, OpaInputProvider<Authentication, ServerWebExchange> inputProvider) {
        return new OpenPolicyAgentPDPClient<>(opaClient, queryProvider, inputProvider);
    }

    @Bean
    @ConditionalOnMissingBean
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
