package com.contentgrid.thunx.encoding.json;

import com.contentgrid.thunx.predicates.model.CollectionValue;
import com.contentgrid.thunx.predicates.model.Scalar;
import com.contentgrid.thunx.predicates.model.ThunkExpression;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(callSuper=true)
@NoArgsConstructor
public class JsonCollectionValueDto extends JsonScalarDto<Collection<JsonScalarDto<?>>> {

    public static final String SET_PROPERTY = "set";
    public static final String ARRAY_PROPERTY = "array";

    private static final Map<Class<?>, String> COLLECTION_TYPES = Map.of(
            Set.class, SET_PROPERTY,
            List.class, ARRAY_PROPERTY
    );

    public JsonCollectionValueDto(String type, Collection<JsonScalarDto<?>> value) {
        super(type, value);
    }

    @Override
    public ThunkExpression<?> toExpression() throws InvalidExpressionDataException {
        switch (this.getType()) {
            case SET_PROPERTY:
                Set<JsonScalarDto<?>> setValues = (Set) super.getValue();
                Set<? extends ThunkExpression<?>> setExpressions =
                        setValues.stream()
                                .map(JsonScalarDto::toExpression)
                                .collect(Collectors.toSet());
                return new CollectionValue((Collection<Scalar<?>>) setExpressions);
            case ARRAY_PROPERTY:
                List<JsonScalarDto<?>> listValues = (List) super.getValue();
                List<? extends ThunkExpression<?>> listExpressions =
                        listValues.stream()
                                .map(JsonScalarDto::toExpression)
                                .collect(Collectors.toList());
                return new CollectionValue((Collection<Scalar<?>>) listExpressions);
            default:
                String message = String.format("Collection of type %s is not supported.", this.getType());
                throw new InvalidExpressionDataException(message);
        }
    }

    @JsonCreator
    public static JsonCollectionValueDto of(@JsonProperty("type") String type, @JsonProperty("value") Collection<JsonScalarDto<?>> value) {
        if (type.equals(SET_PROPERTY)) {
            return new JsonCollectionValueDto(type, value == null ? null : new HashSet<>(value));
        } else if (type.equals(ARRAY_PROPERTY)) {
            return new JsonCollectionValueDto(type, value == null ? null :  new ArrayList<>(value));
        }
        throw new UnsupportedOperationException(String.format("Collection of type %s is not supported.", type));
    }

    public static String getTypeClass(Class type) {
        return COLLECTION_TYPES.entrySet()
                .stream()
                .filter(e -> e.getKey().isAssignableFrom(type))
                .map(Map.Entry::getValue)
                .findFirst().orElseThrow(() ->
                        new UnsupportedOperationException(String.format("This type %s is not supported for collection: %s", type)));
    }
}
