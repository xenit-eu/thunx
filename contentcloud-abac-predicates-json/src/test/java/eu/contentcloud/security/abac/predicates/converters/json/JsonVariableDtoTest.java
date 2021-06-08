package eu.contentcloud.security.abac.predicates.converters.json;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.Test;

class JsonVariableDtoTest {

    private ObjectMapper mapper = new ObjectMapper();

    @Test
    void serialize() throws JsonProcessingException {
        var dto = JsonVariableDto.named("varname");
        var json = mapper.writeValueAsString(dto);

        assertThatJson(json).isEqualTo("{ type: 'var', name: 'varname' }");
    }

    @Test
    void deserialize() throws JsonProcessingException {
        var json = mapper.writeValueAsString(Map.of(
                "type", "var",
                "name", "varname"
        ));

        JsonExpressionDto variable = mapper.readValue(json, JsonExpressionDto.class);
        assertThat(variable)
                .isNotNull()
                .isEqualTo(JsonVariableDto.named("varname"));
    }
}