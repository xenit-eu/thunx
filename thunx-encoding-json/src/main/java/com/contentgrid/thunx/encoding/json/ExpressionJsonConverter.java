package com.contentgrid.thunx.encoding.json;

import com.contentgrid.thunx.predicates.model.ThunkExpression;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;

/**
 * @deprecated use the {@link JsonThunkExpressionCoder} instead
 */
@Deprecated(forRemoval = true, since = "0.10.1")
public class ExpressionJsonConverter {

    private final JsonThunkExpressionCoder coder;


    public ExpressionJsonConverter() {
        this(new ObjectMapper());
    }

    public ExpressionJsonConverter(ObjectMapper objectMapper) {
        this.coder = new JsonThunkExpressionCoder(objectMapper);
    }

    public String encode(ThunkExpression<?> expression) {
        return new String(coder.encode(expression.assertResultType(Boolean.class)), StandardCharsets.UTF_8);
    }


    public ThunkExpression<?> decode(String json) throws InvalidExpressionDataException {
        return this.coder.decode(json.getBytes(StandardCharsets.UTF_8));

    }



}
