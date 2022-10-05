package com.contentgrid.thunx.predicates.model;

import java.math.BigDecimal;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode
class NumberValue implements Scalar<Number> {

    @Getter
    private BigDecimal value;

    NumberValue(BigDecimal value) {
        this.value = value;
    }

    @Override
    public Class<? extends Number> getResultType() {
        return Number.class;
    }

    @Override
    public String toString() {
        return this.getValue().toString();
    }
}
