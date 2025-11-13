package com.contentgrid.thunx.predicates.model;

import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode
public class ListValue implements CollectionValue<List<? extends ThunkExpression<?>>> {

    private final List<? extends ThunkExpression<?>> value;

    public ListValue(List<? extends ThunkExpression<?>> value) {
        this.value = value;
    }

    @Override
    public Class<? extends List<ThunkExpression<?>>> getResultType() {
        return (Class) List.class;
    }

    @Override
    public <R, C> R accept(ThunkExpressionVisitor<R, C> visitor, C context) {
        return visitor.visit(this, context);
    }

    @Override
    public List<? extends ThunkExpression<?>> getValue() {
        return value;
    }
}
