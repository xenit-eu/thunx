package com.contentgrid.thunx.pdp;

import java.util.concurrent.CompletableFuture;

public interface PolicyDecisionComponent<A extends AuthenticationContext, R extends RequestContext> {
    
    CompletableFuture<PolicyDecision> authorize(A principal, R requestContext);
}
