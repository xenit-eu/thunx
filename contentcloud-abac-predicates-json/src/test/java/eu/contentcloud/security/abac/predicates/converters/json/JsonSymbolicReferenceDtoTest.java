package eu.contentcloud.security.abac.predicates.converters.json;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class JsonSymbolicReferenceDtoTest {

    private ObjectMapper mapper = new ObjectMapper();

    @Test
    void serialize() throws JsonProcessingException {
        // answers.life
        var dto = new JsonSymbolicReferenceDto(JsonVariableDto.named("answers"), List.of(JsonScalarDto.of("life")));
        var json = mapper.writeValueAsString(dto);

        assertThatJson(json).isEqualTo("{"
                + "     type: 'ref',"
                + "     subject: { type: 'var', name: 'answers' },"
                + "     path: [{ type: 'string', value: 'life' }] "
                + "}");
    }

    @Test
    void deserialize() throws JsonProcessingException {
        var json = mapper.writeValueAsString(Map.of(
                "type", "ref",
                "subject", Map.of("type", "var", "name", "answers"),
                "path", List.of(Map.of("type", "string", "value", "life"))
        ));

        JsonExpressionDto symbol = mapper.readValue(json, JsonExpressionDto.class);
        assertThat(symbol)
                .isNotNull()
                .isEqualTo(JsonSymbolicReferenceDto.of("answers", "life"));

    }

}