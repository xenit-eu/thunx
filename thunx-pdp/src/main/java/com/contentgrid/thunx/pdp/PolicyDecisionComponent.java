package com.contentgrid.thunx.pdp;

import java.util.concurrent.CompletableFuture;

public interface PolicyDecisionComponent {
    
    CompletableFuture<PolicyDecision> authorize(AuthenticationContext principal, RequestContext requestContext);
}
