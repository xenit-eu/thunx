package eu.xenit.contentcloud.thunx.predicates.converters.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.javacrumbs.jsonunit.providers.Jackson2ObjectMapperProvider;

/**
 * Register a customized Jackson2 ObjectMapper with jsonunit:
 * - fail when detecting duplicate fields
 */
public class CustomObjectMapperProvider implements Jackson2ObjectMapperProvider {
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final ObjectMapper lenientMapper = new ObjectMapper();

    static {
        mapper.enable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION);

        lenientMapper.enable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION);
        lenientMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        lenientMapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        lenientMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);

    }
    @Override
    public ObjectMapper getObjectMapper(boolean lenient) {
        return lenient ? lenientMapper : mapper;
    }
}
