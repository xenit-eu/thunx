package eu.contentcloud.security.abac.pdp;

import reactor.core.publisher.Mono;

public interface PolicyDecisionComponent {

    <TPrincipal> Mono<PolicyDecision> authorize(Mono<TPrincipal> principal, RequestContext requestContext);
}
