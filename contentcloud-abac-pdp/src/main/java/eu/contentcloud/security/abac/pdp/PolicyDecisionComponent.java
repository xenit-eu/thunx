package eu.contentcloud.security.abac.pdp;

import java.util.concurrent.CompletableFuture;

public interface PolicyDecisionComponent {
    
    CompletableFuture<PolicyDecision> authorize(AuthenticationContext principal, RequestContext requestContext);
}
