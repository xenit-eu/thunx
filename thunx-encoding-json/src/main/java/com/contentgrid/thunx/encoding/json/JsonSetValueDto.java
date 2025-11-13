package com.contentgrid.thunx.encoding.json;

import com.contentgrid.thunx.predicates.model.SetValue;
import com.contentgrid.thunx.predicates.model.ThunkExpression;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class JsonSetValueDto implements JsonExpressionDto {

    private final String type = "set";

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Set<JsonExpressionDto> value;

    @Override
    public ThunkExpression<?> toExpression() throws InvalidExpressionDataException {

        Set<? extends ThunkExpression<?>> setExpressions =
                        this.getValue().stream()
                                .map(JsonExpressionDto::toExpression)
                                .collect(Collectors.toSet());
        return new SetValue(setExpressions);
    }

    @JsonCreator
    public static JsonSetValueDto of(@JsonProperty("value") Collection<JsonExpressionDto> value) {
        return new JsonSetValueDto(new HashSet<>(value));
    }

}
