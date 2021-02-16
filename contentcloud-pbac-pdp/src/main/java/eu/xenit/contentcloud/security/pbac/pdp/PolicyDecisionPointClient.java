package eu.xenit.contentcloud.security.pbac.pdp;

import reactor.core.publisher.Mono;

public interface PolicyDecisionPointClient {

    <TPrincipal> Mono<PolicyDecision> conditional(TPrincipal principal, RequestContext requestContext);
}
