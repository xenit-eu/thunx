package eu.xenit.contentcloud.security.pbac.pdp;

import reactor.core.publisher.Mono;

public class PolicyDecisionComponentImpl implements PolicyDecisionComponent {

    private final PolicyDecisionPointClient client;

    public PolicyDecisionComponentImpl(PolicyDecisionPointClient client) {
        this.client = client;
    }

    @Override
    public <TPrincipal> Mono<PolicyDecision> authorize(Mono<TPrincipal> principal, RequestContext requestContext) {
        return this.client.conditional(principal, requestContext);
    }
}
