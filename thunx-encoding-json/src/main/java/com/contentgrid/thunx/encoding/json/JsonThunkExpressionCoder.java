package com.contentgrid.thunx.encoding.json;

import com.contentgrid.thunx.encoding.ThunkExpressionDecoder;
import com.contentgrid.thunx.encoding.ThunkExpressionEncoder;
import com.contentgrid.thunx.predicates.model.CollectionValue;
import com.contentgrid.thunx.predicates.model.ContextFreeThunkExpressionVisitor;
import com.contentgrid.thunx.predicates.model.FunctionExpression;
import com.contentgrid.thunx.predicates.model.Scalar;
import com.contentgrid.thunx.predicates.model.SymbolicReference;
import com.contentgrid.thunx.predicates.model.ThunkExpression;
import com.contentgrid.thunx.predicates.model.Variable;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collection;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

/**
 * JSON encoder and decoder for thunx expressions
 * @since 0.10.1
 */
@RequiredArgsConstructor
public class JsonThunkExpressionCoder implements ThunkExpressionEncoder, ThunkExpressionDecoder {
    private final ObjectMapper objectMapper;
    private static final JsonEncoderVisitor visitor = new JsonEncoderVisitor();

    public JsonThunkExpressionCoder() {
        this(new ObjectMapper());
    }

    public ThunkExpression<?> decodeFromJson(JsonNode node) throws JsonProcessingException {
        return this.objectMapper.treeToValue(node, JsonExpressionDto.class)
                .toExpression();
    }

    /**
     * @deprecated use {@link #decode(byte[])} instead
     */
    @Override
    @Deprecated(forRemoval = true, since = "0.10.1")
    public ThunkExpression<Boolean> decoder(byte[] data) {
        return decode(data);
    }

    @Override
    public ThunkExpression<Boolean> decode(byte[] data) {
        try {
            return decodeFromJson(this.objectMapper.readTree(data))
                    .assertResultType(Boolean.class);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public JsonNode encodeToJson(ThunkExpression<?> expression) {
        var jsonDto = expression.accept(visitor, null);
        return this.objectMapper.valueToTree(jsonDto);
    }

    @Override
    public byte[] encode(ThunkExpression<Boolean> expression) {
        try {
            return this.objectMapper.writeValueAsBytes(encodeToJson(expression));
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static class JsonEncoderVisitor extends ContextFreeThunkExpressionVisitor<JsonExpressionDto> {

        @Override
        public JsonExpressionDto visit(Scalar<?> scalar) {
            return processScalar(scalar);
        }

        @Override
        public JsonExpressionDto visit(FunctionExpression<?> functionExpression) {
            var operator = functionExpression.getOperator();
            var result = functionExpression.getTerms().stream().map(term -> term.accept(this, null));


            return new JsonFunctionDto(operator.getKey(), result);
        }

        @Override
        public JsonExpressionDto visit(SymbolicReference ref) {
            var jsonExprTerms = ref.getPath().stream().map(p -> p.accept(this)).collect(Collectors.toList());
            return new JsonSymbolicReferenceDto(ref.getSubject().accept(this, null), jsonExprTerms);
        }

        @Override
        public JsonExpressionDto visit(Variable variable) {
            return new JsonVariableDto(variable.getName());
        }

        @Override
        protected JsonExpressionDto visit(CollectionValue collectionValue) {
            return processCollectionDto(collectionValue);
        }


    }

    private static class JsonScalarEncoderVisitor extends ContextFreeThunkExpressionVisitor<JsonScalarDto<?>> {

        @Override
        protected JsonScalarDto<?> visit(Scalar<?> scalar) {
            return processScalar(scalar);
        }

        @Override
        protected JsonScalarDto<?> visit(FunctionExpression<?> functionExpression) {
            throw new UnsupportedOperationException("Only Scalars are supported in Collection.");
        }

        @Override
        protected JsonScalarDto<?> visit(SymbolicReference symbolicReference) {
            throw new UnsupportedOperationException("Only Scalars are supported in Collection.");
        }

        @Override
        protected JsonScalarDto<?> visit(Variable variable) {
            throw new UnsupportedOperationException("Only Scalars are supported in Collection.");
        }

        @Override
        protected JsonScalarDto<?> visit(CollectionValue collectionValue) {
            return processCollectionDto(collectionValue);
        }
    }

    private static JsonScalarDto<?> processScalar(Scalar<?> scalar) {
        var resultType = scalar.getResultType();

        var typeName = JsonScalarDto.SCALAR_TYPES.get(resultType);
        if (typeName == null) {
            String exMessage = String.format("Scalar of type <%s> is not supported", resultType.getName());
            throw new IllegalArgumentException(exMessage);
        }

        return JsonScalarDto.of(typeName, scalar.getValue());
    }

    private static JsonScalarDto<?> processCollectionDto(CollectionValue collectionValue) {
        var resultType = collectionValue.getResultType();

        Collection<JsonExpressionDto> result;
        if (JsonCollectionValueDto.SET_PROPERTY
                .equals(JsonCollectionValueDto.getTypeClass(collectionValue.getResultType()))) {
            result = collectionValue.getValue()
                    .stream()
                    .map(scalar -> scalar.accept(new JsonScalarEncoderVisitor(), null))
                    .collect(Collectors.toSet());
        } else if (JsonCollectionValueDto.ARRAY_PROPERTY
                .equals(JsonCollectionValueDto.getTypeClass(collectionValue.getResultType()))) {
            result = collectionValue.getValue()
                    .stream()
                    .map(scalar -> scalar.accept(new JsonScalarEncoderVisitor(), null))
                    .collect(Collectors.toList());
        } else {
            throw new UnsupportedOperationException("Unsupported collection type: " + resultType);
        }

        return JsonCollectionValueDto.of(JsonCollectionValueDto.getTypeClass(resultType), result);
    }
}
