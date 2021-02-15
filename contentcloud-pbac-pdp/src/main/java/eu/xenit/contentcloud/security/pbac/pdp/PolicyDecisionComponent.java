package eu.xenit.contentcloud.security.pbac.pdp;

import reactor.core.publisher.Mono;

public interface PolicyDecisionComponent {

    Mono<PolicyDecision> authorize(/* TODO args */);
}
