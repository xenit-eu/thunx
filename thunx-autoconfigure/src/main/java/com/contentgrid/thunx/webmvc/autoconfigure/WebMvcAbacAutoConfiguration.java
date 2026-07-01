package com.contentgrid.thunx.webmvc.autoconfigure;

import com.contentgrid.opa.client.OpaClient;
import com.contentgrid.thunx.pdp.PolicyDecisionComponent;
import com.contentgrid.thunx.pdp.PolicyDecisionComponentImpl;
import com.contentgrid.thunx.pdp.PolicyDecisionPointClient;
import com.contentgrid.thunx.pdp.opa.OpaInputProvider;
import com.contentgrid.thunx.pdp.opa.OpaQueryProvider;
import com.contentgrid.thunx.pdp.opa.OpenPolicyAgentPDPClient;
import com.contentgrid.thunx.spring.security.AbacContext;
import com.contentgrid.thunx.spring.security.AbacContextSupplier;
import com.contentgrid.thunx.spring.webmvc.AbacContextClearingFilter;
import com.contentgrid.thunx.spring.webmvc.PolicyAuthorizationManager;
import com.contentgrid.thunx.spring.webmvc.ServletOpaInputProvider;
import com.contentgrid.thunx.gateway.autoconfigure.OpaProperties;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

@AutoConfiguration
@ConditionalOnClass({OpaClient.class, PolicyAuthorizationManager.class})
@ConditionalOnWebApplication(type = Type.SERVLET)
@ConditionalOnProperty(value = "contentgrid.thunx.abac.source", havingValue = "opa")
@EnableConfigurationProperties(OpaProperties.class)
public class WebMvcAbacAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public OpaQueryProvider<HttpServletRequest> servletOpaQueryProvider(OpaProperties opaProperties) {
        return request -> opaProperties.getQuery();
    }

    @Bean
    @ConditionalOnMissingBean
    public OpaInputProvider<Authentication, HttpServletRequest> servletOpaInputProvider() {
        return new ServletOpaInputProvider();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(OpaClient.class)
    public PolicyDecisionPointClient<Authentication, HttpServletRequest> servletPdpClient(
            OpaClient opaClient,
            OpaQueryProvider<HttpServletRequest> queryProvider,
            OpaInputProvider<Authentication, HttpServletRequest> inputProvider) {
        return new OpenPolicyAgentPDPClient<>(opaClient, queryProvider, inputProvider);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(PolicyDecisionPointClient.class)
    public PolicyDecisionComponent<Authentication, HttpServletRequest> servletPolicyDecisionComponent(
            PolicyDecisionPointClient<Authentication, HttpServletRequest> pdpClient) {
        return new PolicyDecisionComponentImpl<>(pdpClient);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(PolicyDecisionComponent.class)
    public AuthorizationManager<RequestAuthorizationContext> policyAuthorizationManager(
            PolicyDecisionComponent<Authentication, HttpServletRequest> policyDecisionComponent) {
        return new PolicyAuthorizationManager(policyDecisionComponent);
    }

    @Bean
    @ConditionalOnMissingBean
    public AbacContextClearingFilter abacContextClearingFilter() {
        return new AbacContextClearingFilter();
    }

    @Bean
    public FilterRegistrationBean<AbacContextClearingFilter> abacContextClearingFilterRegistration(
            AbacContextClearingFilter filter) {
        return new FilterRegistrationBean<>(filter);
    }

    @Bean
    @ConditionalOnMissingBean
    public AbacContextSupplier opaAbacContextSupplier() {
        return AbacContext::getCurrentAbacContext;
    }
}
