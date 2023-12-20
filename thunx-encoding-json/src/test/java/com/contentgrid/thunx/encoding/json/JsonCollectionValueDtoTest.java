package com.contentgrid.thunx.encoding.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JsonCollectionValueDtoTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void serialize() throws JsonProcessingException {
        List<JsonScalarDto<?>> scalarDtos = List.of(JsonScalarDto.of(1));
        var dto = JsonCollectionValueDto.of(JsonCollectionValueDto.getTypeByClass(scalarDtos.getClass()), scalarDtos);
        var json = mapper.writeValueAsString(dto);

        assertThatJson(json).isEqualTo("{type: 'array', value: [{type:'number',value: 1}]}");
    }

    @Test
    void deserialize() throws JsonProcessingException {
        var json = mapper.writeValueAsString(Map.of(
                "type", "array",
                "value", List.of(Map.of("type", "number", "value", 1))
        ));

        List<JsonScalarDto<?>> scalarDtos = List.of(JsonScalarDto.of(1));
        var dto = JsonCollectionValueDto.of(JsonCollectionValueDto.getTypeByClass(scalarDtos.getClass()), scalarDtos);
        JsonExpressionDto scalar = mapper.readValue(json, JsonExpressionDto.class);
        assertThat(scalar).isEqualTo(dto);
    }

    @Test
    void deserialize_nullValue() throws JsonProcessingException {
        var json = "{ \"type\": \"array\", \"value\": null }";

        JsonExpressionDto scalar = mapper.readValue(json, JsonExpressionDto.class);
        assertThat(scalar).isEqualTo(new JsonCollectionValueDto("array", null));
    }
}
