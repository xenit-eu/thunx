package com.contentgrid.thunx.encoding.json;

import com.contentgrid.thunx.predicates.model.ThunkExpression;
import java.nio.charset.StandardCharsets;
import tools.jackson.databind.json.JsonMapper;

/**
 * @deprecated use the {@link JsonThunkExpressionCoder} instead
 */
@Deprecated(forRemoval = true, since = "0.10.1")
public class ExpressionJsonConverter {

    private final JsonThunkExpressionCoder coder;


    public ExpressionJsonConverter() {
        this(JsonMapper.builder().build());
    }

    public ExpressionJsonConverter(JsonMapper jsonMapper) {
        this.coder = new JsonThunkExpressionCoder(jsonMapper);
    }

    public String encode(ThunkExpression<?> expression) {
        return new String(coder.encode(expression.assertResultType(Boolean.class)), StandardCharsets.UTF_8);
    }


    public ThunkExpression<?> decode(String json) throws InvalidExpressionDataException {
        return this.coder.decode(json.getBytes(StandardCharsets.UTF_8));

    }



}
