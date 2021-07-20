package eu.xenit.contentcloud.thunx.pdp;

import java.util.concurrent.CompletableFuture;

public interface PolicyDecisionComponent {
    
    CompletableFuture<PolicyDecision> authorize(AuthenticationContext principal, RequestContext requestContext);
}
