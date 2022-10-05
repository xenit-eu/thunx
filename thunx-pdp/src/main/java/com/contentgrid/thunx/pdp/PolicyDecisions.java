package com.contentgrid.thunx.pdp;

import com.contentgrid.thunx.predicates.model.ThunkExpression;

public class PolicyDecisions {

    public static PolicyDecision allowed() {
        return new PolicyDecision(true);
    }

    public static PolicyDecision denied() {
        return new PolicyDecision(false);
    }

    public static PolicyDecision conditional(ThunkExpression<Boolean> predicate) {
        return new PolicyDecision(predicate);
    }
}
