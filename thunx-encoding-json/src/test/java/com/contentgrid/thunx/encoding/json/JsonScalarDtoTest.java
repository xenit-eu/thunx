package com.contentgrid.thunx.encoding.json;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

class JsonScalarDtoTest {

    private final JsonMapper mapper = JsonMapper.builder().build();

    @Nested
    class TypeString {

        @Test
        void serialize() {
            var dto = JsonScalarDto.of("foobar");
            var json = mapper.writeValueAsString(dto);

            assertThatJson(json).isEqualTo("{ type: 'string', value: 'foobar' }");
        }

        @Test
        void deserialize() {
            var json = mapper.writeValueAsString(Map.of(
                    "type", "string",
                    "value", "foobar"
            ));

            JsonExpressionDto scalar = mapper.readValue(json, JsonExpressionDto.class);
            assertThat(scalar).isEqualTo(JsonScalarDto.of("foobar"));
        }

        @Test
        void deserialize_nullValue() {
            var json = "{ \"type\": \"string\", \"value\": null }";

            JsonExpressionDto scalar = mapper.readValue(json, JsonExpressionDto.class);
            assertThat(scalar).isEqualTo(new JsonScalarDto("string", null));
        }
    }

    @Nested
    class TypeNumber {

        @Test
        void serialize() {
            var dto = JsonScalarDto.of(42);
            var json = mapper.writeValueAsString(dto);

            assertThatJson(json).isEqualTo("{ type: 'number', value: 42 }");
        }

        @Test
        void deserialize() {
            var json = mapper.writeValueAsString(Map.of(
                    "type", "number",
                    "value", 42
            ));

            JsonExpressionDto scalar = mapper.readValue(json, JsonExpressionDto.class);
            assertThat(scalar).isEqualTo(JsonScalarDto.of(42));
        }

        @Test
        void deserialize_nullValue() {
            var json = "{ \"type\": \"number\", \"value\": null }";

            JsonExpressionDto scalar = mapper.readValue(json, JsonExpressionDto.class);
            assertThat(scalar).isNotNull().isInstanceOf(JsonScalarDto.class);
            assertThat(scalar).isEqualTo(new JsonScalarDto("number", null));
        }

        @Test
        void deserialize_stringValue() {
            var json = "{ \"type\": \"number\", \"value\": \"invalid\" }";

            JsonExpressionDto scalar = mapper.readValue(json, JsonExpressionDto.class);
            assertThat(scalar).isNotNull().isInstanceOf(JsonScalarDto.class);
            assertThat(scalar).hasFieldOrPropertyWithValue("type", "number");
            assertThat(scalar).hasFieldOrPropertyWithValue("value", "invalid");
        }
    }

    @Nested
    class TypeBoolean {

        @Test
        void serialize() {
            var dto = JsonScalarDto.of(true);
            var json = mapper.writeValueAsString(dto);

            assertThatJson(json).isEqualTo("{ type: 'bool', value: true }");
        }

        @Test
        void deserialize() {
            var json = mapper.writeValueAsString(Map.of(
                    "type", "bool",
                    "value", true
            ));

            JsonExpressionDto scalar = mapper.readValue(json, JsonExpressionDto.class);
            assertThat(scalar).isEqualTo(JsonScalarDto.of(true));
        }

        @Test
        void deserialize_invalidValue_shouldStillSucceed() {
            var json = mapper.writeValueAsString(Map.of(
                    "type", "bool",
                    "value", "invalid"
            ));

            JsonExpressionDto scalar = mapper.readValue(json, JsonExpressionDto.class);
            assertThat(scalar).isNotNull();
            assertThat(scalar).hasFieldOrPropertyWithValue("type", "bool");
            assertThat(scalar).hasFieldOrPropertyWithValue("value", "invalid");
        }
    }

    @Nested
    class TypeNull {

        @Test
        void serialize() {
            var dto = JsonScalarDto.nullValue();
            var json = mapper.writeValueAsString(dto);

            assertThatJson(json).isEqualTo("{ type: 'null' }");
        }

        @Test
        void deserialize() {
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
        void deserialize() {
            var json = mapper.writeValueAsString(Map.of(
                    "type", "invalid",
                    "custom", "unknown"
            ));

            JsonExpressionDto scalar = mapper.readValue(json, JsonExpressionDto.class);
            assertThat(scalar).isNotNull().isInstanceOf(UnknownTypeExpressionDto.class);
            assertThat(scalar).hasFieldOrPropertyWithValue("type", "invalid");
        }
    }


}