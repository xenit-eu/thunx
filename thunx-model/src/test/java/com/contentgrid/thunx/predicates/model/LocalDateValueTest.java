package com.contentgrid.thunx.predicates.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class LocalDateValueTest {

    @Test
    void equals() {
        var now = LocalDate.now();
        assertThat(Scalar.of(now)).isEqualTo(Scalar.of(now));
        assertThat(Scalar.of(LocalDate.parse("2026-01-01")))
                .isEqualTo(Scalar.of(LocalDate.of(2026, 1, 1)));
        assertThat(Scalar.of(LocalDate.ofInstant(Instant.parse("2026-01-01T03:45:12Z"), ZoneOffset.UTC)))
                .isEqualTo(Scalar.of(LocalDate.of(2026, 1, 1)));
    }

    @Test
    void notEquals() {
        assertThat(Scalar.of(LocalDate.parse("2025-01-01"))).isNotEqualTo(Scalar.of(LocalDate.parse("2026-01-01"))); // year
        assertThat(Scalar.of(LocalDate.parse("2026-02-01"))).isNotEqualTo(Scalar.of(LocalDate.parse("2026-01-01"))); // month
        assertThat(Scalar.of(LocalDate.parse("2026-01-02"))).isNotEqualTo(Scalar.of(LocalDate.parse("2026-01-01"))); // day
    }

    @Test
    void localDateValue_toString() {
        assertThat(Scalar.of(LocalDate.parse("2026-01-01"))).hasToString("'2026-01-01'");
    }
}