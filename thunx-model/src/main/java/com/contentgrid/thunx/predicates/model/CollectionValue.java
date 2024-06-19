package com.contentgrid.thunx.predicates.model;

import lombok.EqualsAndHashCode;

import java.util.Collection;

@EqualsAndHashCode(exclude = "type")
public class CollectionValue implements Scalar<Collection<Scalar<?>>> {

    private final Collection<Scalar<?>> value;

    private final Class<? extends Collection<Scalar<?>>> type;

    @Override
    public Collection<Scalar<?>> getValue() {
        return value;
    }

    protected CollectionValue(Collection<Scalar<?>> value, Class<? extends Collection<Scalar<?>>> type) {
        this.value = value;
        this.type = type;
    }

    public CollectionValue(Collection<Scalar<?>> value) {
        this(value, (Class<? extends Collection<Scalar<?>>>)value.getClass());
    }

    @Override
    public Class<? extends Collection<Scalar<?>>> getResultType() {
        return type;
    }

    @Override
    public <R, C> R accept(ThunkExpressionVisitor<R, C> visitor, C context) {
        return visitor.visit(this, context);
    }
}
