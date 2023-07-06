package com.contentgrid.thunx.pdp;

import java.util.concurrent.CompletableFuture;

public class PolicyDecisionComponentImpl<A, R> implements PolicyDecisionComponent<A, R> {

    private final PolicyDecisionPointClient<A, R> client;

    public PolicyDecisionComponentImpl(PolicyDecisionPointClient<A, R> client) {
        this.client = client;
    }

    @Override
    public CompletableFuture<PolicyDecision> authorize(A authContext, R requestContext) {
        return this.client.conditional(authContext, requestContext);
    }
}
