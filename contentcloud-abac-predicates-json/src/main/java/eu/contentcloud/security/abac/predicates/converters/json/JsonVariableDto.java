package eu.contentcloud.security.abac.predicates.converters.json;

import eu.contentcloud.abac.predicates.model.Expression;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
class JsonVariableDto implements JsonExpressionDto {

    private final String type = "var";
    private String name;

    @Override
    public <T> Expression<T> toExpression() {
        return null;
    }
}
