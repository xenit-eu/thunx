package eu.contentcloud.security.abac.predicates.converters.json;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class JsonScalarDtoTest {

    private ObjectMapper mapper = new ObjectMapper();

    @Nested
    class TypeString {

        @Test
        void serialize() throws JsonProcessingException {
            var dto = JsonScalarDto.of("foobar");
            var json = mapper.writeValueAsString(dto);

            assertThatJson(json)
                    .isObject()
                    .hasSize(2)
                    .containsEntry("type", "string")
                    .containsEntry("value", "foobar");
        }

        @Test
        void deserialize() throws JsonProcessingException {
            var json = mapper.writeValueAsString(Map.of(
                    "type", "string",
                    "value", "foobar"
            ));

            JsonScalarDto scalar = mapper.readValue(json, JsonScalarDto.class);
            assertThat(scalar).isNotNull();
            assertThat(scalar.getType()).isEqualTo("string");
            assertThat(scalar.getValue()).isEqualTo("foobar");
        }
    }

    @Nested
    class TypeNumber {

        @Test
        void serialize() throws JsonProcessingException {
            var dto = JsonScalarDto.of(42);
            var json = mapper.writeValueAsString(dto);

            assertThatJson(json)
                    .isObject()
                    .hasSize(2)
                    .containsEntry("type", "number")
                    .containsEntry("value", 42);
        }

        @Test
        void deserialize() throws JsonProcessingException {
            var json = mapper.writeValueAsString(Map.of(
                    "type", "number",
                    "value", 42
            ));

            JsonScalarDto scalar = mapper.readValue(json, JsonScalarDto.class);
            assertThat(scalar).isNotNull();
            assertThat(scalar.getType()).isEqualTo("number");
            assertThat(scalar.getValue()).isEqualTo(42);
        }
    }

}