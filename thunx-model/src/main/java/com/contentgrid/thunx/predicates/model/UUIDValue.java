package com.contentgrid.thunx.predicates.model;

import java.util.UUID;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
@Getter
class UUIDValue implements Scalar<UUID> {

    @NonNull
    private final UUID value;

    @Override
    public Class<? extends UUID> getResultType() {
        return UUID.class;
    }

    @Override
    public String toString() {
        return "'" + this.getValue() + "'";
    }
}
