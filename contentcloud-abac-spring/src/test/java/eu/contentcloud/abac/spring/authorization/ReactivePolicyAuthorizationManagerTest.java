package eu.contentcloud.abac.spring.authorization;

import static org.assertj.core.api.Assertions.assertThat;

import eu.contentcloud.security.abac.pdp.PolicyDecision;
import eu.contentcloud.security.abac.pdp.PolicyDecisionComponentImpl;
import eu.contentcloud.security.abac.pdp.PolicyDecisionPointClient;
import eu.contentcloud.security.abac.pdp.PolicyDecisions;
import eu.contentcloud.security.abac.pdp.RequestContext;
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

class ReactivePolicyAuthorizationManagerTest {

    @Test
    void accessGranted() {
        var authorizationManager = new ReactivePolicyAuthorizationManager(new PolicyDecisionComponentImpl(policySaysYes()));

        var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("http://localhost"));
        var context = new AuthorizationContext(exchange);

        StepVerifier.create(authorizationManager.verify(authentication(), context))
                .expectComplete()
                .verify();
    }

    @Test
    void accessDenied() {
        var authorizationManager = new ReactivePolicyAuthorizationManager(new PolicyDecisionComponentImpl(policySaysNo()));

        var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("http://localhost"));
        var context = new AuthorizationContext(exchange);

        StepVerifier.create(authorizationManager.verify(authentication(), context))
                .expectError(AccessDeniedException.class)
                .verify();
    }

    @Test
    void conditionalAccess() {
        var authorizationManager = new ReactivePolicyAuthorizationManager(new PolicyDecisionComponentImpl(policySaysMaybe()));

        var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("http://localhost"));
        var context = new AuthorizationContext(exchange);

        StepVerifier.create(authorizationManager.verify(authentication(), context))
                .expectComplete()
                .verify();

        Expression<Boolean> obj = context.getExchange().getAttribute(
                ReactivePolicyAuthorizationManager.ABAC_POLICY_PREDICATE_ATTR);
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