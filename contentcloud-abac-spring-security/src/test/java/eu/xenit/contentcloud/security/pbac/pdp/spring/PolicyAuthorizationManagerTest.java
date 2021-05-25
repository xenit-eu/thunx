package eu.xenit.contentcloud.security.pbac.pdp.spring;

import static eu.xenit.contentcloud.security.pbac.pdp.spring.PolicyAuthorizationManager.ABAC_POLICY_PREDICATE_ATTR;
import static org.assertj.core.api.Assertions.assertThat;

import eu.xenit.contentcloud.security.pbac.pdp.PolicyDecision;
import eu.xenit.contentcloud.security.pbac.pdp.PolicyDecisionComponentImpl;
import eu.xenit.contentcloud.security.pbac.pdp.PolicyDecisionPointClient;
import eu.xenit.contentcloud.security.pbac.pdp.PolicyDecisions;
import eu.xenit.contentcloud.security.pbac.pdp.RequestContext;
import eu.xenit.contentcloud.security.pbac.predicates.model.BooleanExpression;
import eu.xenit.contentcloud.security.pbac.predicates.model.Comparison;
import eu.xenit.contentcloud.security.pbac.predicates.model.Expression;
//import eu.xenit.contentcloud.security.pbac.predicates.model.ResolvableBooleanExpression;
import eu.xenit.contentcloud.security.pbac.predicates.model.ExpressionVisitor;
import eu.xenit.contentcloud.security.pbac.predicates.model.Scalar;
import eu.xenit.contentcloud.security.pbac.predicates.model.Variable;
import java.util.Collection;
import java.util.List;
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

        Expression<Boolean> obj = context.getExchange().getAttribute(ABAC_POLICY_PREDICATE_ATTR);
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