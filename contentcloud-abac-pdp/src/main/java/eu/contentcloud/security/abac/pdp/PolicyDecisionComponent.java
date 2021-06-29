package eu.contentcloud.security.abac.pdp;

import reactor.core.publisher.Mono;

public interface PolicyDecisionComponent {

    // TODO refactor to use CompletableFuture instead
    <TPrincipal> Mono<PolicyDecision> authorize(Mono<TPrincipal> principal, RequestContext requestContext);
}
