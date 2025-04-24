package com.contentgrid.thunx.predicates.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;

class InstantValueTest {

    @Test
    void equals() {
        var now = Instant.now();
        assertThat(Scalar.of(now)).isEqualTo(Scalar.of(now));
        assertThat(Scalar.of(Instant.parse("2025-01-01T00:00:00Z"))).isEqualTo(Scalar.of(Instant.parse("2025-01-01T00:00:00.000Z")));
        assertThat(Scalar.of(OffsetDateTime.parse("2024-12-31T23:45:12-03:00").toInstant()))
                .isEqualTo(Scalar.of(OffsetDateTime.parse("2025-01-01T03:45:12+01:00").toInstant()));
    }

    @Test
    void notEquals() {
        assertThat(Scalar.of(Instant.parse("2024-01-01T00:00:00.000Z"))).isNotEqualTo(Scalar.of(Instant.parse("2025-01-01T00:00:00.000Z"))); // year
        assertThat(Scalar.of(Instant.parse("2025-02-01T00:00:00.000Z"))).isNotEqualTo(Scalar.of(Instant.parse("2025-01-01T00:00:00.000Z"))); // month
        assertThat(Scalar.of(Instant.parse("2025-01-02T00:00:00.000Z"))).isNotEqualTo(Scalar.of(Instant.parse("2025-01-01T00:00:00.000Z"))); // day
        assertThat(Scalar.of(Instant.parse("2025-01-01T01:00:00.000Z"))).isNotEqualTo(Scalar.of(Instant.parse("2025-01-01T00:00:00.000Z"))); // hour
        assertThat(Scalar.of(Instant.parse("2025-01-01T00:01:00.000Z"))).isNotEqualTo(Scalar.of(Instant.parse("2025-01-01T00:00:00.000Z"))); // minute
        assertThat(Scalar.of(Instant.parse("2025-01-01T00:00:01.000Z"))).isNotEqualTo(Scalar.of(Instant.parse("2025-01-01T00:00:00.000Z"))); // second
        assertThat(Scalar.of(Instant.parse("2025-01-01T00:00:00.001Z"))).isNotEqualTo(Scalar.of(Instant.parse("2025-01-01T00:00:00.000Z"))); // millisecond
    }

    @Test
    void instantValue_toString() {
        assertThat(Scalar.of(Instant.parse("2025-01-01T00:00:00.000Z"))).hasToString("'2025-01-01T00:00:00Z'");
    }
}