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
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.stream.Collectors;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * JSON encoder and decoder for thunx expressions
 * @since 0.10.1
 */
@RequiredArgsConstructor
public class JsonThunkExpressionCoder implements ThunkExpressionEncoder, ThunkExpressionDecoder {
    private final JsonMapper jsonMapper;
    private static final JsonEncoderVisitor visitor = new JsonEncoderVisitor();

    public JsonThunkExpressionCoder() {
        this(JsonMapper.builder().build());
    }

    public ThunkExpression<?> decodeFromJson(JsonNode node) {
        return this.jsonMapper.treeToValue(node, JsonExpressionDto.class)
                .toExpression();
    }

    @Override
    public ThunkExpression<Boolean> decode(byte[] data) {
        return decodeFromJson(this.jsonMapper.readTree(data))
                .assertResultType(Boolean.class);
    }

    public JsonNode encodeToJson(ThunkExpression<?> expression) {
        var jsonDto = expression.accept(visitor, null);
        return this.jsonMapper.valueToTree(jsonDto);
    }

    @Override
    public byte[] encode(ThunkExpression<Boolean> expression) {
        return this.jsonMapper.writeValueAsBytes(encodeToJson(expression));
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
