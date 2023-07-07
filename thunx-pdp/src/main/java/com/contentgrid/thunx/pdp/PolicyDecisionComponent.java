package com.contentgrid.thunx.pdp;

import java.util.concurrent.CompletableFuture;

public interface PolicyDecisionComponent<A, R> {
    
    CompletableFuture<PolicyDecision> authorize(A principal, R requestContext);
}
