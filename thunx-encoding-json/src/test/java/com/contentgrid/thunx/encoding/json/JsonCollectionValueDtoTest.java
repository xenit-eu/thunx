package com.contentgrid.thunx.encoding.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;

class JsonCollectionValueDtoTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void serialize() throws JsonProcessingException {
        List<JsonExpressionDto> scalarDtos = List.of(JsonScalarDto.of(1));
        var dto = JsonCollectionValueDto.of(JsonCollectionValueDto.getTypeClass(scalarDtos.getClass()), scalarDtos);
        var json = mapper.writeValueAsString(dto);

        assertThatJson(json).isEqualTo("{type: 'array', value: [{type: 'number',value: 1}]}");
    }

    @Test
    void deserialize() throws JsonProcessingException {
        var json = mapper.writeValueAsString(Map.of(
                "type", "array",
                "value", List.of(Map.of("type", "number", "value", 1))
        ));

        List<JsonExpressionDto> scalarDtos = List.of(JsonScalarDto.of(1));
        var dto = JsonCollectionValueDto.of(JsonCollectionValueDto.getTypeClass(scalarDtos.getClass()), scalarDtos);
        JsonExpressionDto scalar = mapper.readValue(json, JsonExpressionDto.class);
        assertThat(scalar).isEqualTo(dto);
    }

    //TODO check if this needs to be tested since null is not allowed for collection
//    @Test
//    void deserialize_nullValue() throws JsonProcessingException {
//        var jsonSet = "{ \"type\": \"set\", \"value\": null }";
//
//        JsonExpressionDto scalarSet = mapper.readValue(jsonSet, JsonExpressionDto.class);
//        assertThat(scalarSet).isEqualTo(new JsonCollectionValueDto("set", null));
//
//        var jsonArray = "{ \"type\": \"array\", \"value\": null }";
//
//        JsonExpressionDto scalarArray = mapper.readValue(jsonArray, JsonExpressionDto.class);
//        assertThat(scalarArray).isEqualTo(new JsonCollectionValueDto("array", null));
//    }
}