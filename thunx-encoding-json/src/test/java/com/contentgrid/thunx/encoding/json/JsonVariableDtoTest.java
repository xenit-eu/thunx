package com.contentgrid.thunx.encoding.json;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

class JsonVariableDtoTest {

    private final JsonMapper mapper = JsonMapper.builder().build();

    @Test
    void serialize() {
        var dto = JsonVariableDto.named("varname");
        var json = mapper.writeValueAsString(dto);

        assertThatJson(json).isEqualTo("{ type: 'var', name: 'varname' }");
    }

    @Test
    void deserialize() {
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