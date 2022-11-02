package com.contentgrid.thunx.predicates.model;

import java.math.BigDecimal;

public interface Scalar<T> extends ThunkExpression<T> {

    T getValue();

    default <R, C> R accept(ThunkExpressionVisitor<R, C> visitor, C context) {
        return visitor.visit(this, context);
    }

    static NumberValue of(BigDecimal number) {
        return new NumberValue(number);
    }

    static NumberValue of(double number) {
        return of(BigDecimal.valueOf(number));
    }

    static NumberValue of(long number) {
        return of(BigDecimal.valueOf(number));
    }

    static StringValue of(String value) {
        return new StringValue(value);
    }

    static BooleanValue of(boolean value) {
        return new BooleanValue(value);
    }

    static NullValue nullValue() {
        return NullValue.INSTANCE;
    }
}
