package com.contentgrid.thunx.predicates.model;

import lombok.EqualsAndHashCode;

import java.util.Collection;

@EqualsAndHashCode
public class CollectionValue implements Scalar<Collection<Scalar<?>>> {

    private final Collection<Scalar<?>> value;

    @Override
    public Collection<Scalar<?>> getValue() {
        return value;
    }

    protected CollectionValue(Collection<Scalar<?>> value) {
        this.value = value;
    }

    @Override
    public Class<? extends Collection<Scalar<?>>> getResultType() {
        return null;
    }

    @Override
    public <R, C> R accept(ThunkExpressionVisitor<R, C> visitor, C context) {
        return visitor.visit(this, context);
    }
}
