package com.contentgrid.thunx.encoding.json;

import com.contentgrid.thunx.predicates.model.ThunkExpression;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = As.EXISTING_PROPERTY,
        property = "type",
        visible = true,
        defaultImpl = UnknownTypeExpressionDto.class)
@JsonSubTypes({
        @Type(value = JsonFunctionDto.class, name = "function"),
        @Type(value = JsonScalarDto.class, name = "string"),
        @Type(value = JsonScalarDto.class, name = "number"),
        @Type(value = JsonScalarDto.class, name = "bool"),
        @Type(value = JsonScalarDto.class, name = "null"),
        @Type(value = JsonVariableDto.class, name = "var"),
        @Type(value = JsonSymbolicReferenceDto.class, name = "ref"),
        @Type(value = JsonCollectionValueDto.class, name = "array"),
        @Type(value = JsonCollectionValueDto.class, name = "set")
})
public interface JsonExpressionDto {

    String getType();

    ThunkExpression<?> toExpression() throws InvalidExpressionDataException;
}
