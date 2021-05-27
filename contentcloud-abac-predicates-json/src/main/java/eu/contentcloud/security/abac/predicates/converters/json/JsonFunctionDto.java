package eu.contentcloud.security.abac.predicates.converters.json;

import eu.contentcloud.abac.predicates.model.Expression;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
class JsonFunctionDto implements JsonExpressionDto {

    private final String type = "function";
    private String operator;

    private final Collection<JsonExpressionDto> terms = new ArrayList<>();

    public JsonFunctionDto(String operator, Collection<JsonExpressionDto> terms) {
        this.operator = operator;
        this.terms.addAll(terms);
    }

    public JsonFunctionDto(String operator, Stream<JsonExpressionDto> result) {
        this.operator = operator;
        result.forEach(expr -> terms.add(expr));
    }

    @Override
    public <T> Expression<T> toExpression() {
        return null;
    }
}
