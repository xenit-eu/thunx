package com.contentgrid.thunx.predicates.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class BooleanValueTest {
    @Test
    void equals() {
        assertThat(Scalar.of(true)).isEqualTo(Scalar.of(true));
        assertThat(Scalar.of(false)).isEqualTo(Scalar.of(false));

    }

    @Test
    void notEquals_byValue() {
        assertThat(Scalar.of(true)).isNotEqualTo(Scalar.of(false));
        assertThat(Scalar.of(false)).isNotEqualTo(Scalar.of(true));
    }

    @Test
    void nullValue_notAllowed() {
        assertThatThrownBy(() -> Scalar.of((Boolean) null))
            .isInstanceOf(NullPointerException.class);
    }
}