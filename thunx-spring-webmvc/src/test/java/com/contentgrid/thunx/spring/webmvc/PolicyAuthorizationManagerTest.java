package com.contentgrid.thunx.spring.webmvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.contentgrid.thunx.pdp.PolicyDecision;
import com.contentgrid.thunx.pdp.PolicyDecisionComponentImpl;
import com.contentgrid.thunx.pdp.PolicyDecisionPointClient;
import com.contentgrid.thunx.pdp.PolicyDecisions;
import com.contentgrid.thunx.predicates.model.Comparison;
import com.contentgrid.thunx.predicates.model.Scalar;
import com.contentgrid.thunx.predicates.model.ThunkExpression;
import com.contentgrid.thunx.predicates.model.Variable;
import com.contentgrid.thunx.spring.security.AbacContext;
import jakarta.servlet.http.HttpServletRequest;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

class PolicyAuthorizationManagerTest {

    @AfterEach
    void clearAbacContext() {
        AbacContext.clear();
    }

    @Test
    void accessGranted() {
        var authorizationManager = new PolicyAuthorizationManager(
                new PolicyDecisionComponentImpl<>(policySaysYes()));

        var context = new RequestAuthorizationContext(new MockHttpServletRequest());

        var decision = authorizationManager.authorize(authentication(), context);

        assertThat(decision.isGranted()).isTrue();
        // context should be updated with the default ABAC predicate
        assertThat(AbacContext.getCurrentAbacContext())
                .isEqualTo(Comparison.areEqual(Scalar.of(true), Scalar.of(true)));
    }

    @Test
    void accessDenied() {
        var authorizationManager = new PolicyAuthorizationManager(
                new PolicyDecisionComponentImpl<>(policySaysNo()));

        var context = new RequestAuthorizationContext(new MockHttpServletRequest());

        var decision = authorizationManager.authorize(authentication(), context);

        assertThat(decision.isGranted()).isFalse();
        assertThat(AbacContext.getCurrentAbacContext()).isNull();
    }

    @Test
    void conditionalAccess() {
        var thunk = Comparison.areEqual(Scalar.of(42), Variable.named("foo"));
        var authorizationManager = new PolicyAuthorizationManager(
                new PolicyDecisionComponentImpl<>(policySaysMaybe(thunk)));

        var context = new RequestAuthorizationContext(new MockHttpServletRequest());

        // access is conditional:
        // - decision should be granted
        // - context should be updated with the ABAC predicate
        var decision = authorizationManager.authorize(authentication(), context);

        assertThat(decision.isGranted()).isTrue();
        assertThat(AbacContext.getCurrentAbacContext()).isEqualTo(thunk);
    }

    @Test
    void authorize_interruptedException_deniesAndRestoresInterruptStatus() {
        var authorizationManager = new PolicyAuthorizationManager(
                new PolicyDecisionComponentImpl<>(policySaysInterrupted()));

        var context = new RequestAuthorizationContext(new MockHttpServletRequest());

        try {
            var decision = authorizationManager.authorize(authentication(), context);

            assertThat(decision.isGranted()).isFalse();
            assertThat(AbacContext.getCurrentAbacContext()).isNull();
            assertThat(Thread.currentThread().isInterrupted()).isTrue();
        } finally {
            Thread.interrupted(); // clear the flag so it doesn't leak into other tests
        }
    }

    @Test
    void authorize_executionException_wrapsAndRethrowsCause() {
        var cause = new RuntimeException("opa down");
        var authorizationManager = new PolicyAuthorizationManager(
                new PolicyDecisionComponentImpl<>(policySaysFailed(cause)));

        var context = new RequestAuthorizationContext(new MockHttpServletRequest());

        var authentication = authentication();
        assertThatThrownBy(() -> authorizationManager.authorize(authentication, context))
                .isInstanceOf(RuntimeException.class)
                .hasCause(cause);
        assertThat(AbacContext.getCurrentAbacContext()).isNull();
    }

    private static Supplier<Authentication> authentication() {
        return () -> new TestingAuthenticationToken("mario", null);
    }

    private static PolicyDecisionPointClient<Authentication, HttpServletRequest> policySaysYes() {
        return (authentication, request) -> CompletableFuture.completedFuture(PolicyDecisions.allowed());
    }

    private static PolicyDecisionPointClient<Authentication, HttpServletRequest> policySaysNo() {
        return (authentication, request) -> CompletableFuture.completedFuture(PolicyDecisions.denied());
    }

    private static PolicyDecisionPointClient<Authentication, HttpServletRequest> policySaysMaybe(
            ThunkExpression<Boolean> expression) {
        return (authentication, request) -> CompletableFuture.completedFuture(PolicyDecisions.conditional(expression));
    }

    private static PolicyDecisionPointClient<Authentication, HttpServletRequest> policySaysInterrupted() {
        return (authentication, request) -> new CompletableFuture<>() {
            @Override
            public PolicyDecision get() throws InterruptedException {
                throw new InterruptedException("interrupted while waiting for policy decision");
            }
        };
    }

    private static PolicyDecisionPointClient<Authentication, HttpServletRequest> policySaysFailed(Throwable cause) {
        return (authentication, request) -> CompletableFuture.failedFuture(cause);
    }
}
