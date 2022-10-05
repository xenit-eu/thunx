package com.contentgrid.thunx.pdp;

import java.util.concurrent.CompletableFuture;

public interface PolicyDecisionPointClient {

    CompletableFuture<PolicyDecision> conditional(AuthenticationContext authContext, RequestContext requestContext);

}
