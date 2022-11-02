package com.contentgrid.thunx.predicates.model;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Getter
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Variable implements ThunkExpression<Object> {

    @NonNull
    private final String name;

    @Override
    public Class<?> getResultType() {
        return Object.class;
    }

    @Override
    public <R, C> R accept(ThunkExpressionVisitor<R, C> visitor, C context) {
        return visitor.visit(this, context);
    }

    public static Variable named(String name) {
        return new Variable(name);
    }
}
