package eu.xenit.contentcloud.thunx.pdp;

import java.util.concurrent.CompletableFuture;

public interface PolicyDecisionPointClient {

    CompletableFuture<PolicyDecision> conditional(AuthenticationContext authContext, RequestContext requestContext);

}
