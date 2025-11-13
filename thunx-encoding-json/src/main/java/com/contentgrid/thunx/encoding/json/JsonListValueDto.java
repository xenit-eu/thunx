package com.contentgrid.thunx.encoding.json;

import com.contentgrid.thunx.predicates.model.ListValue;
import com.contentgrid.thunx.predicates.model.ThunkExpression;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class JsonListValueDto implements JsonExpressionDto {

    private final String type = "array";

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<JsonExpressionDto> value;

    @Override
    public ThunkExpression<?> toExpression() throws InvalidExpressionDataException {
        List<? extends ThunkExpression<?>> listExpressions =
                this.getValue().stream()
                        .map(JsonExpressionDto::toExpression)
                        .collect(Collectors.toList());
        return new ListValue(listExpressions);
    }

    @JsonCreator
    public static JsonListValueDto of(@JsonProperty("value") Collection<JsonExpressionDto> value) {
        return new JsonListValueDto(new ArrayList<>(value));
    }

}
