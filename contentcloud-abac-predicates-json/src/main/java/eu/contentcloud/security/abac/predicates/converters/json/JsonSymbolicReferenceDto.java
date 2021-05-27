package eu.contentcloud.security.abac.predicates.converters.json;

import eu.contentcloud.abac.predicates.model.Expression;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
class JsonSymbolicReferenceDto implements JsonExpressionDto {

    private final String type = "ref";
    private JsonExpressionDto subject;
    private List<JsonExpressionDto> path;

    public JsonSymbolicReferenceDto(JsonExpressionDto subject, List<JsonExpressionDto> path) {
        super();
        this.subject = subject;
        this.path = path;
    }

    @Override
    public <T> Expression<T> toExpression() {
        return null;
    }
}
