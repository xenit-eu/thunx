package com.contentgrid.thunx.encoding.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;

class JsonSetValueDtoTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void serialize() throws JsonProcessingException {
        Set<JsonExpressionDto> scalarDtos = Set.of(JsonScalarDto.of(1));
        var dto = JsonSetValueDto.of(scalarDtos);
        var json = mapper.writeValueAsString(dto);

        assertThatJson(json).isEqualTo("{type: 'set', value: [{type: 'number',value: 1}]}");
    }

    @Test
    void deserialize() throws JsonProcessingException {
        var json = mapper.writeValueAsString(Map.of(
                "type", "set",
                "value", Set.of(Map.of("type", "number", "value", 1))
        ));

        Set<JsonExpressionDto> scalarDtos = Set.of(JsonScalarDto.of(1));
        var dto = JsonSetValueDto.of(scalarDtos);
        JsonExpressionDto scalar = mapper.readValue(json, JsonExpressionDto.class);
        assertThat(scalar).isEqualTo(dto);
    }

}