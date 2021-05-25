package eu.xenit.contentcloud.security.pbac.pdp;

import eu.xenit.contentcloud.security.pbac.predicates.model.Expression;

public class PolicyDecision {

    private final boolean allowed;
    private final Expression<Boolean> predicate;

    PolicyDecision(boolean allowed) {
        this.allowed = allowed;
        this.predicate = null;
    }

    PolicyDecision(Expression<Boolean> predicate) {
        this.allowed = true;
        this.predicate = predicate;
    }

    public boolean hasPredicate() {
        return this.predicate != null;
    }

    public Expression<Boolean> getPredicate() {
        return this.predicate;
    }

    public boolean isAllowed() {
        return this.allowed;
    }

}
