package com.contentgrid.thunx.predicates.model;

import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
@Getter
class InstantValue implements Scalar<Instant> {

    @NonNull
    private final Instant value;

    @Override
    public Class<? extends Instant> getResultType() {
        return Instant.class;
    }

    @Override
    public String toString() {
        return "'" + this.getValue() + "'";
    }
}
