package com.contentgrid.thunx.spring.webmvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.contentgrid.thunx.pdp.PolicyDecision;
import com.contentgrid.thunx.pdp.PolicyDecisionComponent;
import com.contentgrid.thunx.pdp.PolicyDecisionComponentImpl;
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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

@ExtendWith(MockitoExtension.class)
class PolicyAuthorizationManagerTest {

    private final MockHttpServletRequest request = new MockHttpServletRequest();
    private final RequestAuthorizationContext context = new RequestAuthorizationContext(request);

    @Mock
    private PolicyDecisionComponent<Authentication, HttpServletRequest> policyDecisionComponent;

    @AfterEach
    void clearAbacContext() {
        AbacContext.clear();
    }

    private static Supplier<Authentication> authentication() {
        return () -> new TestingAuthenticationToken("mario", null);
    }

    private static PolicyAuthorizationManager managerReturning(PolicyDecision decision) {
        PolicyDecisionComponentImpl<Authentication, HttpServletRequest> component =
                new PolicyDecisionComponentImpl<>((auth, req) -> CompletableFuture.completedFuture(decision));
        return new PolicyAuthorizationManager(component);
    }

    @Test
    void authorize_allowedWithoutPredicate_grantsAndSetsDefaultAbacContext() {
        var manager = managerReturning(PolicyDecisions.allowed());

        var decision = manager.authorize(authentication(), context);

        assertThat(decision.isGranted()).isTrue();
        assertThat(AbacContext.getCurrentAbacContext())
                .isEqualTo(Comparison.areEqual(Scalar.of(true), Scalar.of(true)));
    }

    @Test
    void authorize_allowedWithPredicate_grantsAndSetsAbacContextToPredicate() {
        ThunkExpression<Boolean> predicate = Comparison.areEqual(Scalar.of(42), Variable.named("foo"));
        var manager = managerReturning(PolicyDecisions.conditional(predicate));

        var decision = manager.authorize(authentication(), context);

        assertThat(decision.isGranted()).isTrue();
        assertThat(AbacContext.getCurrentAbacContext()).isEqualTo(predicate);
    }

    @Test
    void authorize_denied_doesNotGrant() {
        var manager = managerReturning(PolicyDecisions.denied());

        var decision = manager.authorize(authentication(), context);

        assertThat(decision.isGranted()).isFalse();
    }

    @Test
    void authorize_interruptedException_deniesAndRestoresInterruptStatus() {
        var manager = new PolicyAuthorizationManager(policyDecisionComponent);
        var future = new CompletableFuture<PolicyDecision>() {
            @Override
            public PolicyDecision get() throws InterruptedException {
                throw new InterruptedException("interrupted while waiting for policy decision");
            }
        };
        when(policyDecisionComponent.authorize(any(), any())).thenReturn(future);

        try {
            var decision = manager.authorize(authentication(), context);

            assertThat(decision.isGranted()).isFalse();
            assertThat(Thread.currentThread().isInterrupted()).isTrue();
        } finally {
            Thread.interrupted(); // clear the flag so it doesn't leak into other tests
        }
    }

    @Test
    void authorize_executionException_wrapsAndRethrowsCause() {
        var manager = new PolicyAuthorizationManager(policyDecisionComponent);
        var cause = new RuntimeException("opa down");
        var authentication = authentication();
        when(policyDecisionComponent.authorize(any(), any()))
                .thenReturn(CompletableFuture.failedFuture(cause));

        assertThatThrownBy(() -> manager.authorize(authentication, context))
                .isInstanceOf(RuntimeException.class)
                .hasCause(cause);
    }
}
