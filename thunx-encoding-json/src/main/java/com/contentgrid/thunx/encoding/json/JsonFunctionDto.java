package com.contentgrid.thunx.encoding.json;

import com.contentgrid.thunx.predicates.model.FunctionExpression;
import com.contentgrid.thunx.predicates.model.FunctionExpression.Operator;
import com.contentgrid.thunx.predicates.model.ThunkExpression;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
class JsonFunctionDto implements JsonExpressionDto {

    private final String type = "function";
    private String operator;

    private List<JsonExpressionDto> terms = new ArrayList<>();

    public JsonFunctionDto(String operator, List<JsonExpressionDto> terms) {
        this.operator = operator;
        this.terms.addAll(terms);
    }

    public JsonFunctionDto(String operator, Stream<JsonExpressionDto> result) {
        this.operator = operator;
        result.forEach(expr -> terms.add(expr));
    }

    public static JsonFunctionDto of(String operator, JsonExpressionDto left, JsonExpressionDto right) {
        return new JsonFunctionDto(operator, List.of(left, right));
    }

    @Override
    public ThunkExpression<?> toExpression() throws InvalidExpressionDataException {

        // convert all the terms
        var exprTerms = new ArrayList<ThunkExpression<?>>();
        for (var dto : terms) {
            exprTerms.add(dto.toExpression());
        }
        var op = Operator.resolve(this.operator);
        return op.create(terms.stream().map(JsonExpressionDto::toExpression).collect(Collectors.toList()));
    }
}
