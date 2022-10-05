package com.contentgrid.thunx.predicates.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

@EqualsAndHashCode(callSuper = false)
class BooleanValue implements Scalar<Boolean> {

    @Getter
    @NonNull
    private Boolean value;

    protected BooleanValue(Boolean value) {
        this.value = value;
    }

    @Override
    public Class<? extends Boolean> getResultType() {
        return Boolean.class;
    }

    @Override
    public String toString() {
        return String.format("%s(%s)", BooleanValue.class.getSimpleName(), this.getValue());
    }
}
