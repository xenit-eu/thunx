package eu.contentcloud.security.abac.predicates.converters.json;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class JsonFunctionDtoTest {

    private ObjectMapper mapper = new ObjectMapper();

    @Test
    void serialize() throws JsonProcessingException {
        // answer == 42
        var dto = JsonFunctionDto.of("eq", JsonVariableDto.named("answer"), JsonScalarDto.of(42));
        var json = mapper.writeValueAsString(dto);

        assertThatJson(json).isEqualTo("{"
                + " type: 'function',"
                + " operator: 'eq',"
                + " terms: [{ type: 'var', name: 'answer' }, { type: 'number', value: 42 }]"
                + "}");
    }

    @Test
    void deserialize() throws JsonProcessingException {
        var json = mapper.writeValueAsString(Map.of(
                "type", "function",
                "operator", "eq",
                "terms", List.of(
                        Map.of("type", "var", "name", "answer"),
                        Map.of("type", "number", "value", 42))
        ));

        JsonExpressionDto function = mapper.readValue(json, JsonExpressionDto.class);
        assertThat(function)
                .isNotNull()
                .isEqualTo(JsonFunctionDto.of("eq", JsonVariableDto.named("answer"), JsonScalarDto.of(42)));
    }

}