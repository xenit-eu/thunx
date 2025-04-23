package com.contentgrid.thunx.predicates.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class UUIDValueTest {

    @Test
    void equals() {
        assertThat(Scalar.of(UUID.fromString("4d88ab31-4456-4b92-a58d-50638cf2966a")))
                .isEqualTo(Scalar.of(UUID.fromString("4d88ab31-4456-4b92-a58d-50638cf2966a")));
        var uuid = UUID.randomUUID();
        assertThat(Scalar.of(uuid)).isEqualTo(Scalar.of(uuid));
    }

    @Test
    void notEquals() {
        assertThat(Scalar.of(UUID.fromString("4d88ab31-4456-4b92-a58d-50638cf2966a")))
                .isNotEqualTo(Scalar.of(UUID.fromString("5e423874-d80d-4b31-8f50-8b34b249a511")));
    }

    @Test
    void uuidValue_toString() {
        assertThat(Scalar.of(UUID.fromString("4d88ab31-4456-4b92-a58d-50638cf2966a")).toString())
                .isEqualTo("'4d88ab31-4456-4b92-a58d-50638cf2966a'");
    }
}