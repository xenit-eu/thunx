package com.contentgrid.thunx.spring.security;

import com.contentgrid.thunx.pdp.PolicyDecision;
import com.contentgrid.thunx.pdp.PolicyDecisionComponent;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.ReactiveAuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import reactor.core.publisher.Mono;

public class ReactivePolicyAuthorizationManager implements ReactiveAuthorizationManager<AuthorizationContext> {

    public static final String ABAC_POLICY_PREDICATE_ATTR = "ABAC_POLICY_PREDICATE";

    private final PolicyDecisionComponent<Authentication, ServerHttpRequest> policyDecisionComponent;

    public ReactivePolicyAuthorizationManager(PolicyDecisionComponent<Authentication, ServerHttpRequest> policyDecisionComponent) {
        this.policyDecisionComponent = policyDecisionComponent;
    }

    @Override
    public Mono<AuthorizationDecision> check(
            Mono<Authentication> authentication, AuthorizationContext authzContext) {
        return authentication.flatMap(authContext ->
                {
                    var policyDecisionFuture = policyDecisionComponent.authorize(authContext, authzContext.getExchange().getRequest());
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

}
