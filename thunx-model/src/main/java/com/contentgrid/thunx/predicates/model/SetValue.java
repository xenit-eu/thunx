package com.contentgrid.thunx.predicates.model;

import lombok.EqualsAndHashCode;

import java.util.Set;

@EqualsAndHashCode
public class SetValue implements CollectionValue<Set<? extends ThunkExpression<?>>> {

    private final Set<? extends ThunkExpression<?>> value;

    public SetValue(Set<? extends ThunkExpression<?>> value) {
        this.value = value;
    }

    @Override
    public Class<? extends Set<ThunkExpression<?>>> getResultType() {
        return (Class) Set.class;
    }


    @Override
    public <R, C> R accept(ThunkExpressionVisitor<R, C> visitor, C context) {
        return visitor.visit(this, context);
    }

    @Override
    public Set<? extends ThunkExpression<?>> getValue() {
        return this.value;
    }

}
