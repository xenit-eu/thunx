package com.contentgrid.thunx.encoding.json;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.contentgrid.thunx.encoding.json.InvalidExpressionDataException.InvalidExpressionValueException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class JsonScalarDtoTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Nested
    class TypeString {

        @Test
        void serialize() throws JsonProcessingException {
            var dto = JsonScalarDto.of("foobar");
            var json = mapper.writeValueAsString(dto);

            assertThatJson(json).isEqualTo("{ type: 'string', value: 'foobar' }");
        }

        @Test
        void deserialize() throws JsonProcessingException {
            var json = mapper.writeValueAsString(Map.of(
                    "type", "string",
                    "value", "foobar"
            ));

            JsonExpressionDto scalar = mapper.readValue(json, JsonExpressionDto.class);
            assertThat(scalar).isEqualTo(JsonScalarDto.of("foobar"));
        }
    }

    @Nested
    class TypeNumber {

        @Test
        void serialize() throws JsonProcessingException {
            var dto = JsonScalarDto.of(42);
            var json = mapper.writeValueAsString(dto);

            assertThatJson(json).isEqualTo("{ type: 'number', value: 42 }");
        }

        @Test
        void deserialize() throws JsonProcessingException {
            var json = mapper.writeValueAsString(Map.of(
                    "type", "number",
                    "value", 42
            ));

            JsonExpressionDto scalar = mapper.readValue(json, JsonExpressionDto.class);
            assertThat(scalar).isEqualTo(JsonScalarDto.of(42));
        }
    }

    @Nested
    class TypeBoolean {

        @Test
        void serialize() throws JsonProcessingException {
            var dto = JsonScalarDto.of(true);
            var json = mapper.writeValueAsString(dto);

            assertThatJson(json).isEqualTo("{ type: 'bool', value: true }");
        }

        @Test
        void deserialize() throws JsonProcessingException {
            var json = mapper.writeValueAsString(Map.of(
                    "type", "bool",
                    "value", true
            ));

            JsonExpressionDto scalar = mapper.readValue(json, JsonExpressionDto.class);
            assertThat(scalar).isEqualTo(JsonScalarDto.of(true));
        }
    }

    @Nested
    class TypeLocalDate {

        @Test
        void serialize() throws JsonProcessingException {
            var dto = JsonScalarDto.of(LocalDate.parse("2026-01-01"));
            var json = mapper.writeValueAsString(dto);

            assertThatJson(json).isEqualTo("{ type: 'date', value: '2026-01-01' }");
        }

        @Test
        void deserialize() throws JsonProcessingException {
            var json = mapper.writeValueAsString(Map.of(
                    "type", "date",
                    "value", "2026-01-01"
            ));

            JsonExpressionDto scalar = mapper.readValue(json, JsonExpressionDto.class);
            assertThat(scalar).isEqualTo(JsonScalarDto.of(LocalDate.parse("2026-01-01")));
        }
    }

    @Nested
    class TypeInstant {

        @Test
        void serialize() throws JsonProcessingException {
            var dto = JsonScalarDto.of(Instant.parse("2026-01-01T01:01:01.001Z"));
            var json = mapper.writeValueAsString(dto);

            assertThatJson(json).isEqualTo("{ type: 'instant', value: '2026-01-01T01:01:01.001Z' }");
        }

        @Test
        void deserialize() throws JsonProcessingException {
            var json = mapper.writeValueAsString(Map.of(
                    "type", "instant",
                    "value", "2026-01-01T01:01:01.001Z"
            ));

            JsonExpressionDto scalar = mapper.readValue(json, JsonExpressionDto.class);
            assertThat(scalar).isEqualTo(JsonScalarDto.of(Instant.parse("2026-01-01T01:01:01.001Z")));
        }
    }

    @Nested
    class TypeUUID {

        @Test
        void serialize() throws JsonProcessingException {
            var dto = JsonScalarDto.of(UUID.fromString("20e53408-1e1b-46b8-a22b-be2868437552"));
            var json = mapper.writeValueAsString(dto);

            assertThatJson(json).isEqualTo("{ type: 'uuid', value: '20e53408-1e1b-46b8-a22b-be2868437552' }");
        }

        @Test
        void deserialize() throws JsonProcessingException {
            var json = mapper.writeValueAsString(Map.of(
                    "type", "uuid",
                    "value", "20e53408-1e1b-46b8-a22b-be2868437552"
            ));

            JsonExpressionDto scalar = mapper.readValue(json, JsonExpressionDto.class);
            assertThat(scalar).isEqualTo(JsonScalarDto.of(UUID.fromString("20e53408-1e1b-46b8-a22b-be2868437552")));
        }
    }

    @Nested
    class TypeNull {

        @Test
        void serialize() throws JsonProcessingException {
            var dto = JsonScalarDto.nullValue();
            var json = mapper.writeValueAsString(dto);

            assertThatJson(json).isEqualTo("{ type: 'null' }");
        }

        @Test
        void deserialize() throws JsonProcessingException {
            var json = mapper.writeValueAsString(Map.of(
                    "type", "null"
            ));

            JsonExpressionDto scalar = mapper.readValue(json, JsonExpressionDto.class);
            assertThat(scalar).isEqualTo(JsonScalarDto.nullValue());

        }
    }

    @Nested
    class InvalidType {

        @Test
        void deserialize() throws JsonProcessingException {
            var json = mapper.writeValueAsString(Map.of(
                    "type", "invalid",
                    "custom", "unknown"
            ));

            JsonExpressionDto scalar = mapper.readValue(json, JsonExpressionDto.class);
            assertThat(scalar).isNotNull().isInstanceOf(UnknownTypeExpressionDto.class);
            assertThat(scalar).hasFieldOrPropertyWithValue("type", "invalid");
        }
    }

    @Nested
    class InvalidValue {

        @ParameterizedTest
        @CsvSource({"string", "number", "bool", "date", "instant", "uuid"})
        void deserialize_nullValue(String type) throws JsonProcessingException {
            var json = "{ \"type\": \"%s\", \"value\": null }".formatted(type);

            JsonExpressionDto scalar = mapper.readValue(json, JsonExpressionDto.class);
            assertThat(scalar).isNotNull().isInstanceOf(JsonScalarDto.class);
            assertThat(scalar).isEqualTo(new JsonScalarDto(type, null));
            assertThatThrownBy(scalar::toExpression)
                    .isInstanceOf(InvalidExpressionValueException.class);
        }

        @ParameterizedTest
        @CsvSource({"number", "bool", "date", "instant", "uuid"})
        void deserialize_stringValue(String type) throws JsonProcessingException {
            var json = "{ \"type\": \"%s\", \"value\": \"invalid\" }".formatted(type);

            JsonExpressionDto scalar = mapper.readValue(json, JsonExpressionDto.class);
            assertThat(scalar).isNotNull().isInstanceOf(JsonScalarDto.class);
            assertThat(scalar).hasFieldOrPropertyWithValue("type", type);
            assertThat(scalar).hasFieldOrPropertyWithValue("value", "invalid");
            assertThatThrownBy(scalar::toExpression)
                    .isInstanceOf(InvalidExpressionValueException.class);
        }
    }


}