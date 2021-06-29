package eu.contentcloud.security.abac.pdp;

import reactor.core.publisher.Mono;

public class PolicyDecisionComponentImpl implements PolicyDecisionComponent {

    private final PolicyDecisionPointClient client;

    public PolicyDecisionComponentImpl(PolicyDecisionPointClient client) {
        this.client = client;
    }

    @Override
    public <TPrincipal> Mono<PolicyDecision> authorize(Mono<TPrincipal> principal, RequestContext requestContext) {
        return principal.flatMap(user -> this.client.conditional(user, requestContext));
    }
}
