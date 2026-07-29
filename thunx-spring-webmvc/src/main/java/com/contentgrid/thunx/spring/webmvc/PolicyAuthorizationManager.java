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
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

@Slf4j
@RequiredArgsConstructor
public class PolicyAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

    private final PolicyDecisionComponent<Authentication, HttpServletRequest> policyDecisionComponent;

    @Override
    public AuthorizationResult authorize(Supplier<? extends Authentication> authentication, RequestAuthorizationContext context) {
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
