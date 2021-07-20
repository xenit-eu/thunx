package eu.xenit.contentcloud.thunx.pdp;

import eu.xenit.contentcloud.thunx.predicates.model.ThunkExpression;

public class PolicyDecision {

    private final boolean allowed;
    private final ThunkExpression<Boolean> predicate;

    PolicyDecision(boolean allowed) {
        this.allowed = allowed;
        this.predicate = null;
    }

    PolicyDecision(ThunkExpression<Boolean> predicate) {
        this.allowed = true;
        this.predicate = predicate;
    }

    public boolean hasPredicate() {
        return this.predicate != null;
    }

    public ThunkExpression<Boolean> getPredicate() {
        return this.predicate;
    }

    public boolean isAllowed() {
        return this.allowed;
    }

}
