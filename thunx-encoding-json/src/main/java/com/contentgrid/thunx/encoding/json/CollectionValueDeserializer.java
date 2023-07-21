package com.contentgrid.thunx.encoding.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class CollectionValueDeserializer extends StdDeserializer {

    private final ObjectMapper objectMapper;

    public CollectionValueDeserializer() {
        this(null);
    }

    public CollectionValueDeserializer(Class<?> vc) {
        super(vc);
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(JsonExpressionDto.class, this);
        mapper.registerModule(module);
        this.objectMapper = mapper;
    }


    @Override
    public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        String type = node.get("type").asText();

        if ("array".equals(type)) {
            List<JsonScalarDto<?>> list = new ArrayList<>();
            Iterator<JsonNode> values = node.get("value").elements();

            while (values.hasNext()) {
                JsonNode value = values.next();
                list.add(deserializeInnerValue(value));
            }
            return new JsonCollectionValueDto("array", list);
        } else if ("set".equals(type)) {
            Set<JsonScalarDto<?>> set = new HashSet<>();

            Iterator<JsonNode> values = node.get("value").elements();
            while (values.hasNext()) {
                JsonNode value = values.next();
                set.add(deserializeInnerValue(value));
            }

            return new JsonCollectionValueDto("set", set);
        }

        throw new IllegalArgumentException("Unsupported type: " + type);
    }

    private JsonScalarDto<?> deserializeInnerValue(JsonNode data) {
        return (JsonScalarDto<?>) objectMapper.convertValue(data, JsonExpressionDto.class);
    }
}
