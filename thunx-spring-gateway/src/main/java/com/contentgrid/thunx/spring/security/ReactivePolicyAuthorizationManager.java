package com.contentgrid.thunx.spring.security;

import com.contentgrid.thunx.pdp.PolicyDecision;
import com.contentgrid.thunx.pdp.PolicyDecisionComponent;
import com.contentgrid.thunx.predicates.model.Comparison;
import com.contentgrid.thunx.predicates.model.Scalar;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.ReactiveAuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public class ReactivePolicyAuthorizationManager implements ReactiveAuthorizationManager<AuthorizationContext> {

    public static final String ABAC_POLICY_PREDICATE_ATTR = "ABAC_POLICY_PREDICATE";

    private final PolicyDecisionComponent<Authentication, ServerWebExchange> policyDecisionComponent;

    public ReactivePolicyAuthorizationManager(PolicyDecisionComponent<Authentication, ServerWebExchange> policyDecisionComponent) {
        this.policyDecisionComponent = policyDecisionComponent;
    }

    @Override
    public Mono<AuthorizationDecision> check(
            Mono<Authentication> authentication, AuthorizationContext authzContext) {
        return authentication.flatMap(authContext ->
                {
                    var policyDecisionFuture = policyDecisionComponent.authorize(authContext, authzContext.getExchange());
                    return Mono.fromCompletionStage(policyDecisionFuture);
                })
                .map((PolicyDecision policyDecision) -> {
                    // policyDecision outcome has 3 cases:
                    // - true
                    // - false
                    // - conditions

                    if (policyDecision.isAllowed()) {
                        var attrs = authzContext.getExchange().getAttributes();
                        if (policyDecision.hasPredicate()) {
                            // partial evaluation!
                            attrs.put(ABAC_POLICY_PREDICATE_ATTR, policyDecision.getPredicate());
                        } else {
                            // Put default: true = true
                            attrs.put(ABAC_POLICY_PREDICATE_ATTR, Comparison.areEqual(Scalar.of(true), Scalar.of(true)));
                        }

                        return new AuthorizationDecision(true);
                    } else {
                        return new AuthorizationDecision(false);
                    }
                });
    }

}
