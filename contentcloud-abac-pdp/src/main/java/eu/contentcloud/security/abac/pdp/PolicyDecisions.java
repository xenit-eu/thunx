package eu.contentcloud.security.abac.pdp;

import eu.contentcloud.abac.predicates.model.Expression;

public class PolicyDecisions {

    public static PolicyDecision allowed() {
        return new PolicyDecision(true);
    }

    public static PolicyDecision denied() {
        return new PolicyDecision(false);
    }

    public static PolicyDecision conditional(Expression<Boolean> predicate) {
        return new PolicyDecision(predicate);
    }
}
