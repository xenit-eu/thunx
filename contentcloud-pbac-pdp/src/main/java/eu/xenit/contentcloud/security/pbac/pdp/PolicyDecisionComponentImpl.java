package eu.xenit.contentcloud.security.pbac.pdp;

import reactor.core.publisher.Mono;

public class PolicyDecisionComponentImpl implements PolicyDecisionComponent {

    private final PolicyDecisionPointClient client;

    public PolicyDecisionComponentImpl(PolicyDecisionPointClient client) {
        this.client = client;
    }

    @Override
    public Mono<PolicyDecision> authorize() {
        return null;
    }
}
