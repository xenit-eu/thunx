package com.contentgrid.thunx.predicates.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class StringValueTest {

    @Test
    void equals() {
        assertThat(Scalar.of("foo"))
                .isEqualTo(Scalar.of("foo"));
    }

    @Test
    void notEquals() {
        assertThat(Scalar.of("foo"))
                .isNotEqualTo(Scalar.of("bar"));
    }

    @Test
    void stringValue_toString() {
        assertThat(Scalar.of("foo")).hasToString("'foo'");
    }
}