package eu.contentcloud.security.abac.pdp;

import java.util.concurrent.CompletableFuture;

public interface PolicyDecisionPointClient {

    CompletableFuture<PolicyDecision> conditional(AuthenticationContext authContext, RequestContext requestContext);

}
