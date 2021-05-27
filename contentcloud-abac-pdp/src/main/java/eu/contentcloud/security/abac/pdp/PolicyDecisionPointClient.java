package eu.contentcloud.security.abac.pdp;

import reactor.core.publisher.Mono;

public interface PolicyDecisionPointClient {

    <TPrincipal> Mono<PolicyDecision> conditional(TPrincipal principal, RequestContext requestContext);
}
