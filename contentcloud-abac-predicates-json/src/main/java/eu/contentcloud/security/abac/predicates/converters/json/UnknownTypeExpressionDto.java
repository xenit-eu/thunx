package eu.contentcloud.security.abac.predicates.converters.json;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import eu.contentcloud.abac.predicates.model.Expression;
import java.util.Map;
import lombok.Data;

@Data
public class UnknownTypeExpressionDto implements JsonExpressionDto {

    private String type;

    @JsonAnySetter
    private Map<String, Object> fields;

    @Override
    public <T> Expression<T> toExpression() {
        return null;
    }
}
