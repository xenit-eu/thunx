package com.contentgrid.thunx.encoding.json;

import com.contentgrid.thunx.predicates.model.ThunkExpression;
import com.contentgrid.thunx.predicates.model.Variable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
class JsonVariableDto implements JsonExpressionDto {

    private final String type = "var";

    private String name;

    public static JsonVariableDto named(String name) {
        return new JsonVariableDto(name);
    }

    @Override
    public ThunkExpression<?> toExpression() {
        return Variable.named(this.name);
    }
}
