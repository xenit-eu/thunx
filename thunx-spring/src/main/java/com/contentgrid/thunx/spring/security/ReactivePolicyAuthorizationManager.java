package com.contentgrid.thunx.spring.security;

import com.contentgrid.thunx.pdp.AuthenticationContext;
import com.contentgrid.thunx.pdp.PolicyDecision;
import com.contentgrid.thunx.pdp.PolicyDecisionComponent;
import com.contentgrid.thunx.pdp.RequestContext;
import com.contentgrid.thunx.spring.security.AuthenticationContextMapper.DefaultAuthenticationContext;
import java.net.URI;
import java.util.List;
import java.util.Map;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.ReactiveAuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public class ReactivePolicyAuthorizationManager implements ReactiveAuthorizationManager<AuthorizationContext> {

    public static final String ABAC_POLICY_PREDICATE_ATTR = "ABAC_POLICY_PREDICATE";

    private final PolicyDecisionComponent<AuthenticationContext, RequestContext> policyDecisionComponent;

    public ReactivePolicyAuthorizationManager(PolicyDecisionComponent<AuthenticationContext, RequestContext> policyDecisionComponent) {
        this.policyDecisionComponent = policyDecisionComponent;
    }

    @Override
    public Mono<AuthorizationDecision> check(
            Mono<Authentication> authentication, AuthorizationContext authzContext) {
        return authentication.map(auth -> AuthenticationContextMapper.fromAuthentication(auth))
                .flatMap(authContext ->
                {
                    var policyDecisionFuture = policyDecisionComponent
                            .authorize(authContext, mapRequestContext(authzContext));
                    return Mono.fromCompletionStage(policyDecisionFuture);
                })
                .map((PolicyDecision policyDecision) -> {
                    // policyDecision outcome has 3 cases:
                    // - true
                    // - false
                    // - conditions

                    if (policyDecision.isAllowed()) {
                        if (policyDecision.hasPredicate()) {
                            // partial evaluation!
                            var attrs = authzContext.getExchange().getAttributes();
                            attrs.put(ABAC_POLICY_PREDICATE_ATTR, policyDecision.getPredicate());
                        }

                        return new AuthorizationDecision(true);
                    } else {
                        return new AuthorizationDecision(false);
                    }
                });
    }

    static AuthenticationContext mapAuthenticationContext(@NonNull Authentication authentication) {
        return AuthenticationContextMapper.fromAuthentication(authentication);
    }

    static RequestContext mapRequestContext(AuthorizationContext context) {
        return new ServerWebExchangeRequestContext(context.getExchange());
    }

}
