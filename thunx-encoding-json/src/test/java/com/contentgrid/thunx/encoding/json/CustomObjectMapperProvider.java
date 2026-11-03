package com.contentgrid.thunx.encoding.json;

import net.javacrumbs.jsonunit.providers.Jackson3ObjectMapperProvider;
import tools.jackson.core.StreamReadFeature;
import tools.jackson.core.json.JsonReadFeature;
import tools.jackson.databind.json.JsonMapper;

/**
 * Register a customized Jackson3 {@link JsonMapper} with jsonunit:
 * - fail when detecting duplicate fields
 */
public class CustomObjectMapperProvider implements Jackson3ObjectMapperProvider {

    private static final JsonMapper mapper = JsonMapper.builder()
            .enable(StreamReadFeature.STRICT_DUPLICATE_DETECTION)
            .build();

    private static final JsonMapper lenientMapper = JsonMapper.builder()
            .enable(StreamReadFeature.STRICT_DUPLICATE_DETECTION)
            .enable(JsonReadFeature.ALLOW_UNQUOTED_PROPERTY_NAMES)
            .enable(JsonReadFeature.ALLOW_JAVA_COMMENTS)
            .enable(JsonReadFeature.ALLOW_SINGLE_QUOTES)
            .build();

    @Override
    public JsonMapper getObjectMapper(boolean lenient) {
        return lenient ? lenientMapper : mapper;
    }
}
