package com.contentgrid.thunx.predicates.model;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
class NullValue implements Scalar<Void> {

    protected static NullValue INSTANCE = new NullValue();

    private NullValue() {
    }

    @Override
    public Void getValue() {
        return null;
    }

    @Override
    public Class<? extends Void> getResultType() {
        return Void.class;
    }
}
