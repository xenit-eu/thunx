package com.contentgrid.thunx.encoding.json;

import com.contentgrid.thunx.encoding.ThunkExpressionDecoder;
import com.contentgrid.thunx.encoding.ThunkExpressionEncoder;
import com.contentgrid.thunx.predicates.model.ContextFreeThunkExpressionVisitor;
import com.contentgrid.thunx.predicates.model.FunctionExpression;
import com.contentgrid.thunx.predicates.model.ListValue;
import com.contentgrid.thunx.predicates.model.Scalar;
import com.contentgrid.thunx.predicates.model.SetValue;
import com.contentgrid.thunx.predicates.model.SymbolicReference;
import com.contentgrid.thunx.predicates.model.ThunkExpression;
import com.contentgrid.thunx.predicates.model.Variable;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collection;
import java.util.stream.Collectors;

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
        protected JsonExpressionDto visit(SetValue setValue) {
            Collection<JsonExpressionDto> result = setValue.getValue()
                    .stream()
                    .map(expression -> expression.accept(new JsonEncoderVisitor(), null))
                    .collect(Collectors.toSet());

            return JsonSetValueDto.of(result);
        }

        @Override
        protected JsonExpressionDto visit(ListValue listValue) {
            Collection<JsonExpressionDto> result = listValue.getValue()
                    .stream()
                    .map(scalar -> scalar.accept(new JsonEncoderVisitor(), null))
                    .collect(Collectors.toList());

            return JsonListValueDto.of(result);
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

}
