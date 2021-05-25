package eu.contentcloud.abac.spring;

import static org.assertj.core.api.Assertions.assertThat;

import eu.contentcloud.security.pbac.pdp.PolicyDecision;
import eu.contentcloud.security.pbac.pdp.PolicyDecisionComponentImpl;
import eu.contentcloud.security.pbac.pdp.PolicyDecisionPointClient;
import eu.contentcloud.security.pbac.pdp.PolicyDecisions;
import eu.contentcloud.security.pbac.pdp.RequestContext;
import eu.contentcloud.abac.predicates.model.Comparison;
import eu.contentcloud.abac.predicates.model.Expression;
import eu.contentcloud.abac.predicates.model.Scalar;
import eu.contentcloud.abac.predicates.model.Variable;
import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class PolicyAuthorizationManagerTest {

    @Test
    void accessGranted() {
        var authorizationManager = new PolicyAuthorizationManager(new PolicyDecisionComponentImpl(policySaysYes()));

        var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("http://localhost"));
        var context = new AuthorizationContext(exchange);

        StepVerifier.create(authorizationManager.verify(authentication(), context))
                .expectComplete()
                .verify();
    }

    @Test
    void accessDenied() {
        var authorizationManager = new PolicyAuthorizationManager(new PolicyDecisionComponentImpl(policySaysNo()));

        var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("http://localhost"));
        var context = new AuthorizationContext(exchange);

        StepVerifier.create(authorizationManager.verify(authentication(), context))
                .expectError(AccessDeniedException.class)
                .verify();
    }

    @Test
    void conditionalAccess() {
        var authorizationManager = new PolicyAuthorizationManager(new PolicyDecisionComponentImpl(policySaysMaybe()));

        var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("http://localhost"));
        var context = new AuthorizationContext(exchange);

        StepVerifier.create(authorizationManager.verify(authentication(), context))
                .expectComplete()
                .verify();

        Expression<Boolean> obj = context.getExchange().getAttribute(
                PolicyAuthorizationManager.ABAC_POLICY_PREDICATE_ATTR);
        assertThat(obj).isNotNull();
    }

    private static Mono<Authentication> authentication() {
        return Mono.just(new TestingAuthenticationToken("mario", null));
    }

    private static PolicyDecisionPointClient policySaysYes() {
        return new PolicyDecisionPointClient() {
            @Override
            public <TPrincipal> Mono<PolicyDecision> conditional(TPrincipal principal, RequestContext requestContext) {
                return Mono.just(PolicyDecisions.allowed());
            }
        };
    }

    private static PolicyDecisionPointClient policySaysNo() {
        return new PolicyDecisionPointClient() {
            @Override
            public <TPrincipal> Mono<PolicyDecision> conditional(TPrincipal principal, RequestContext requestContext) {
                return Mono.just(PolicyDecisions.denied());
            }
        };
    }

    private static PolicyDecisionPointClient policySaysMaybe() {
        return new PolicyDecisionPointClient() {
            @Override
            public <TPrincipal> Mono<PolicyDecision> conditional(TPrincipal principal, RequestContext requestContext) {
                return Mono.just(PolicyDecisions.conditional(
                        Comparison.areEqual(Scalar.of(42), Variable.named("foo"))
                ));
            }
        };
    }
}