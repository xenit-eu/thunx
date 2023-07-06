package com.contentgrid.thunx.pdp;

import java.util.concurrent.CompletableFuture;

public interface PolicyDecisionPointClient<A, R> {

    CompletableFuture<PolicyDecision> conditional(A authContext, R requestContext);

}
