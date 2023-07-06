package com.contentgrid.thunx.pdp;

import java.util.concurrent.CompletableFuture;

public interface PolicyDecisionPointClient<A extends AuthenticationContext, R extends RequestContext> {

    CompletableFuture<PolicyDecision> conditional(A authContext, R requestContext);

}
