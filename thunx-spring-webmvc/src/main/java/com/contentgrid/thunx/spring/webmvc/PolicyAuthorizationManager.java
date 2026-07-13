package com.contentgrid.thunx.spring.webmvc;

import com.contentgrid.thunx.pdp.PolicyDecisionComponent;
import com.contentgrid.thunx.predicates.model.Comparison;
import com.contentgrid.thunx.predicates.model.Scalar;
import com.contentgrid.thunx.spring.security.AbacContext;
import jakarta.servlet.http.HttpServletRequest;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

@Slf4j
@RequiredArgsConstructor
public class PolicyAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

    private final PolicyDecisionComponent<Authentication, HttpServletRequest> policyDecisionComponent;

    // Spring Security 6.x dispatches via check() (abstract); 7.x removed check() and dispatches via
    // authorize() instead. Implementing both keeps this correct regardless of which major version is
    // actually on the runtime classpath of the consuming application.
    @Override
    public AuthorizationDecision check(Supplier<Authentication> authentication, RequestAuthorizationContext context) {
        return authorize(authentication, context);
    }

    @Override
    public AuthorizationDecision authorize(Supplier<Authentication> authentication, RequestAuthorizationContext context) {
        var currentAbacContext = AbacContext.getCurrentAbacContext();
        if (currentAbacContext != null) {
            log.warn("Abac Context was not clear before running the OPA authorize, clearing it.");
            AbacContext.clear();
        }
        try {
            var policyDecision = policyDecisionComponent
                    .authorize(authentication.get(), context.getRequest())
                    .get();
            // policyDecision outcome has 3 cases:
            // - true
            // - false
            // - conditions

            if (policyDecision.isAllowed()) {
                if (policyDecision.hasPredicate()) {
                    // partial evaluation!
                    AbacContext.setCurrentAbacContext(policyDecision.getPredicate());
                } else {
                    // Put default: true = true
                    AbacContext.setCurrentAbacContext(Comparison.areEqual(Scalar.of(true), Scalar.of(true)));
                }
                return new AuthorizationDecision(true);
            } else {
                return new AuthorizationDecision(false);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new AuthorizationDecision(false);
        } catch (ExecutionException e) {
            throw new RuntimeException("Failed to obtain policy decision from OPA", e.getCause());
        }
    }
}
