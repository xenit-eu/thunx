package com.contentgrid.thunx.pdp;

import java.util.concurrent.CompletableFuture;

public class PolicyDecisionComponentImpl implements PolicyDecisionComponent {

    private final PolicyDecisionPointClient client;

    public PolicyDecisionComponentImpl(PolicyDecisionPointClient client) {
        this.client = client;
    }

    @Override
    public CompletableFuture<PolicyDecision> authorize(AuthenticationContext authContext, RequestContext requestContext) {
        return this.client.conditional(authContext, requestContext);
    }
}
