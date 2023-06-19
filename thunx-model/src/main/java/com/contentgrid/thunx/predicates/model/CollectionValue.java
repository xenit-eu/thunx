package com.contentgrid.thunx.predicates.model;

import com.contentgrid.opa.rego.ast.Term;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Collection;

@EqualsAndHashCode
public class CollectionValue implements Scalar<Collection<Term.ScalarTerm<Object>>> {

    @Getter
    private final Collection<Term.ScalarTerm<Object>> value;

    protected CollectionValue(Collection<Term.ScalarTerm<Object>> value) {
        this.value = value;
    }

    @Override
    public Class<? extends Collection<Term.ScalarTerm<Object>>> getResultType() {
        return null; // TODO: return correct class
    }

    @Override
    public <R, C> R accept(ThunkExpressionVisitor<R, C> visitor, C context) {
        return visitor.visit(this, context);
    }
}
