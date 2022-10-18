package com.contentgrid.thunx.encoding.json;

import com.contentgrid.thunx.predicates.model.ThunkExpression;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import java.util.Map;
import lombok.Data;

@Data
public class UnknownTypeExpressionDto implements JsonExpressionDto {

    private String type;

    @JsonAnySetter
    private Map<String, Object> fields;

    @Override
    public ThunkExpression<?> toExpression() {
        return null;
    }
}
