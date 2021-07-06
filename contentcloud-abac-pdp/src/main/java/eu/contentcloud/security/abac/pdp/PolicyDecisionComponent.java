package eu.contentcloud.security.abac.pdp;

import java.util.concurrent.CompletableFuture;

public interface PolicyDecisionComponent {

    // TODO refactor to use CompletableFuture instead
    CompletableFuture<PolicyDecision> authorize(AuthenticationContext principal, RequestContext requestContext);
}
