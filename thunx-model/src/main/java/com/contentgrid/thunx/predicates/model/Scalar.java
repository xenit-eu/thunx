package com.contentgrid.thunx.predicates.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public interface Scalar<T> extends ThunkExpression<T> {

    T getValue();

    default <R, C> R accept(ThunkExpressionVisitor<R, C> visitor, C context) {
        return visitor.visit(this, context);
    }

    static Scalar<Number> of(BigDecimal number) {
        return new NumberValue(number);
    }

    static Scalar<Number> of(double number) {
        return of(BigDecimal.valueOf(number));
    }

    static Scalar<Number> of(long number) {
        return of(BigDecimal.valueOf(number));
    }

    static Scalar<String> of(String value) {
        return new StringValue(value);
    }

    static Scalar<Boolean> of(boolean value) {
        return new BooleanValue(value);
    }

    static Scalar<Instant> of(Instant value) {
        return new InstantValue(value);
    }

    static Scalar<LocalDate> of(LocalDate value) {
        return new LocalDateValue(value);
    }

    static Scalar<UUID> of(UUID value) {
        return new UUIDValue(value);
    }

    static Scalar<Void> nullValue() {
        return NullValue.INSTANCE;
    }
}
