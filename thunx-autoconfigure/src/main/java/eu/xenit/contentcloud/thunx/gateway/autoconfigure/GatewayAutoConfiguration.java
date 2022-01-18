package eu.xenit.contentcloud.thunx.gateway.autoconfigure;

import eu.xenit.contentcloud.opa.client.OpaClient;
import eu.xenit.contentcloud.opa.client.rest.RestClientConfiguration;
import eu.xenit.contentcloud.thunx.pdp.PolicyDecisionComponentImpl;
import eu.xenit.contentcloud.thunx.pdp.PolicyDecisionPointClient;
import eu.xenit.contentcloud.thunx.pdp.opa.OpaQueryProvider;
import eu.xenit.contentcloud.thunx.pdp.opa.OpenPolicyAgentPDPClient;
import eu.xenit.contentcloud.thunx.spring.gateway.filter.AbacGatewayFilterFactory;
import eu.xenit.contentcloud.thunx.spring.security.ReactivePolicyAuthorizationManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authorization.ReactiveAuthorizationManager;
import org.springframework.security.web.server.authorization.AuthorizationContext;

@Configuration
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
    public OpaQueryProvider propertyBasedOpaQueryProvider(OpaProperties opaProperties) {
        return request -> opaProperties.getQuery();
    }

    @Bean
    @ConditionalOnMissingBean
    public PolicyDecisionPointClient pdpClient(OpaClient opaClient, OpaQueryProvider queryProvider) {
        return new OpenPolicyAgentPDPClient(opaClient, queryProvider);
    }

    @Bean
    @ConditionalOnMissingBean
    public ReactiveAuthorizationManager<AuthorizationContext> reactiveAuthenticationManager(
            PolicyDecisionPointClient pdpClient) {
        return new ReactivePolicyAuthorizationManager(new PolicyDecisionComponentImpl(pdpClient));
    }

    @Bean
    @ConditionalOnMissingBean
    public AbacGatewayFilterFactory abacGatewayFilterFactory() {
        return new AbacGatewayFilterFactory();
    }
}
