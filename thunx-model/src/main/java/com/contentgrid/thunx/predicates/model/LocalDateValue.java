package com.contentgrid.thunx.predicates.model;

import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
@Getter
class LocalDateValue implements Scalar<LocalDate> {

    @NonNull
    private final LocalDate value;

    @Override
    public Class<? extends LocalDate> getResultType() {
        return LocalDate.class;
    }

    @Override
    public String toString() {
        return "'" + this.getValue() + "'";
    }
}
