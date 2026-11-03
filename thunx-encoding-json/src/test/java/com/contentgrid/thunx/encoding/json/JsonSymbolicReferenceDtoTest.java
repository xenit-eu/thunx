package com.contentgrid.thunx.encoding.json;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

class JsonSymbolicReferenceDtoTest {

    private final JsonMapper mapper = JsonMapper.builder().build();

    @Test
    void serialize() {
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
    void deserialize() {
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