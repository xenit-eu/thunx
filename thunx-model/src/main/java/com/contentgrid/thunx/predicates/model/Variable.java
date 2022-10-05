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
    public <R> R accept(ThunkExpressionVisitor<R> visitor) {
        return visitor.visit(this);
    }

    public static Variable named(String name) {
        return new Variable(name);
    }
}
