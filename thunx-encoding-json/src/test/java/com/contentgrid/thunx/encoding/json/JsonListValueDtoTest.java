package com.contentgrid.thunx.encoding.json;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import tools.jackson.databind.json.JsonMapper;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;

class JsonListValueDtoTest {

    private final JsonMapper mapper = JsonMapper.builder().build();

    @Test
    void serialize() {
        List<JsonExpressionDto> scalarDtos = List.of(JsonScalarDto.of(1));
        var dto = JsonListValueDto.of(scalarDtos);
        var json = mapper.writeValueAsString(dto);

        assertThatJson(json).isEqualTo("{type: 'array', value: [{type: 'number',value: 1}]}");
    }

    @Test
    void deserialize() {
        var json = mapper.writeValueAsString(Map.of(
                "type", "array",
                "value", List.of(Map.of("type", "number", "value", 1))
        ));

        List<JsonExpressionDto> scalarDtos = List.of(JsonScalarDto.of(1));
        var dto = JsonListValueDto.of(scalarDtos);
        JsonExpressionDto scalar = mapper.readValue(json, JsonExpressionDto.class);
        assertThat(scalar).isEqualTo(dto);
    }

}