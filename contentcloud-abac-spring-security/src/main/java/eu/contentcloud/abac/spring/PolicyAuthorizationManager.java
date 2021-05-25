package eu.contentcloud.abac.spring;

import eu.contentcloud.security.pbac.pdp.PolicyDecision;
import eu.contentcloud.security.pbac.pdp.PolicyDecisionComponent;
import eu.contentcloud.security.pbac.pdp.RequestContext;
import java.net.URI;
import java.util.List;
import java.util.Map;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.ReactiveAuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import reactor.core.publisher.Mono;

public class PolicyAuthorizationManager implements ReactiveAuthorizationManager<AuthorizationContext> {

    public static final String ABAC_POLICY_PREDICATE_ATTR = "ABAC_POLICY_PREDICATE";

    private final PolicyDecisionComponent policyDecisionComponent;

    public PolicyAuthorizationManager(PolicyDecisionComponent policyDecisionComponent) {
        this.policyDecisionComponent = policyDecisionComponent;
    }

    @Override
    public Mono<AuthorizationDecision> check(
            Mono<Authentication> authentication, AuthorizationContext authzContext) {
        return policyDecisionComponent.authorize(authentication, mapRequestContext(authzContext))
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

    private static RequestContext mapRequestContext(AuthorizationContext context) {
        return new RequestContext() {

            @Override
            public String getHttpMethod() {
                return context.getExchange().getRequest().getMethodValue();
            }

            @Override
            public URI getURI() {
                return context.getExchange().getRequest().getURI();
            }

            @Override
            public Map<String, List<String>> getQueryParams() {
                return context.getExchange().getRequest().getQueryParams();
            }
        };
    }
}
