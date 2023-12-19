package com.contentgrid.thunx.spring.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.contentgrid.thunx.pdp.PolicyDecisionComponentImpl;
import com.contentgrid.thunx.pdp.PolicyDecisionPointClient;
import com.contentgrid.thunx.pdp.PolicyDecisions;
import com.contentgrid.thunx.predicates.model.Comparison;
import com.contentgrid.thunx.predicates.model.Scalar;
import com.contentgrid.thunx.predicates.model.ThunkExpression;
import com.contentgrid.thunx.predicates.model.Variable;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class ReactivePolicyAuthorizationManagerTest {

    @Test
    void accessGranted() {
        var authorizationManager = new ReactivePolicyAuthorizationManager(
                new PolicyDecisionComponentImpl<>(policySaysYes()));

        var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("http://localhost"));
        var context = new AuthorizationContext(exchange);

        StepVerifier.create(authorizationManager.verify(authentication(), context))
                .expectComplete()
                .verify();
    }

    @Test
    void accessDenied() {
        var authorizationManager = new ReactivePolicyAuthorizationManager(
                new PolicyDecisionComponentImpl<>(policySaysNo()));

        var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("http://localhost"));
        var context = new AuthorizationContext(exchange);

        StepVerifier.create(authorizationManager.verify(authentication(), context))
                .expectError(AccessDeniedException.class)
                .verify();
    }

    @Test
    void conditionalAccess() {
        var thunk = Comparison.areEqual(Scalar.of(42), Variable.named("foo"));
        var pdpClient = policySaysMaybe(thunk);

        var authorizationManager = new ReactivePolicyAuthorizationManager(new PolicyDecisionComponentImpl<>(pdpClient));

        var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("http://localhost"));
        var context = new AuthorizationContext(exchange);

        // the authorization manager returns a Mono<Void>
        // the access here is conditional:
        // - mono should complete normally
        // - context should be updated with an 'ABAC_POLICY_PREDICATE' attribute
        StepVerifier.create(authorizationManager.verify(authentication(), context))
                .expectComplete()
                .verify();

        ThunkExpression<Boolean> predicateAttr = context.getExchange().getAttribute(
                ReactivePolicyAuthorizationManager.ABAC_POLICY_PREDICATE_ATTR);
        assertThat(predicateAttr)
                .isNotNull()
                .isEqualTo(Comparison.areEqual(Scalar.of(42), Variable.named("foo")));
    }

    private static Mono<Authentication> authentication() {
        return Mono.just(new TestingAuthenticationToken("mario", null));
    }

    private static PolicyDecisionPointClient<Authentication, ServerWebExchange> policySaysYes() {
        return (authentication, exchange) -> CompletableFuture.completedFuture(PolicyDecisions.allowed());
    }

    private static PolicyDecisionPointClient<Authentication, ServerWebExchange> policySaysNo() {
        return (authentication, exchange) -> CompletableFuture.completedFuture(PolicyDecisions.denied());
    }

    private static PolicyDecisionPointClient<Authentication, ServerWebExchange> policySaysMaybe(ThunkExpression<Boolean> expression) {
        return (authentication, exchange) -> CompletableFuture.completedFuture(PolicyDecisions.conditional(expression));
    }
}