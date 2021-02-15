package eu.xenit.contentcloud.security.pbac.pdp.spring;

import eu.xenit.contentcloud.security.pbac.pdp.PolicyDecisionComponent;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.ReactiveAuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import reactor.core.publisher.Mono;

public class PolicyAuthorizationManager implements ReactiveAuthorizationManager<AuthorizationContext> {

    private PolicyDecisionComponent policyDecision;

    @Override
    public Mono<AuthorizationDecision> check(
            Mono<Authentication> authentication, AuthorizationContext object) {
        return null;
    }
}
