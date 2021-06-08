package eu.contentcloud.security.abac.predicates.converters.json;

import eu.contentcloud.abac.predicates.model.Expression;
import eu.contentcloud.abac.predicates.model.Variable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
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
    public Expression<?> toExpression() {
        return Variable.named(this.name);
    }
}
