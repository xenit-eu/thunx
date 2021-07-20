package eu.xenit.contentcloud.thunx.predicates.converters.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.xenit.contentcloud.thunx.predicates.model.Expression;
import eu.xenit.contentcloud.thunx.predicates.model.SymbolicReference;
import eu.xenit.contentcloud.thunx.predicates.model.Variable;
import eu.xenit.contentcloud.thunx.predicates.model.ExpressionVisitor;
import eu.xenit.contentcloud.thunx.predicates.model.FunctionExpression;
import eu.xenit.contentcloud.thunx.predicates.model.Scalar;
import java.io.UncheckedIOException;
import java.util.stream.Collectors;

public class ExpressionJsonConverter {

    private final ObjectMapper objectMapper;
    private final JsonEncoderVisitor visitor = new JsonEncoderVisitor();

    public ExpressionJsonConverter() {
        this(new ObjectMapper());
    }

    public ExpressionJsonConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String encode(Expression<?> expression) {
        try {
            JsonExpressionDto jsonDto = expression.accept(this.visitor);
            return this.objectMapper.writeValueAsString(jsonDto);
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
    }

    public Expression<?> decode(String json) throws JsonProcessingException, InvalidExpressionDataException {
        var dto = this.objectMapper.readValue(json, JsonExpressionDto.class);

        return dto.toExpression();

    }

    private static class JsonEncoderVisitor implements ExpressionVisitor<JsonExpressionDto> {

        @Override
        public JsonExpressionDto visit(Scalar<?> scalar) {
            var resultType = scalar.getResultType();

            var typeName = JsonScalarDto.SCALAR_TYPES.get(resultType);
            if (typeName == null) {
                String exMessage = String.format("Scalar of type <%s> is not supported", resultType.getName());
                throw new IllegalArgumentException(exMessage);
            }

            return JsonScalarDto.of(typeName, scalar.getValue());
        }

        @Override
        public JsonExpressionDto visit(FunctionExpression<?> functionExpression) {
            var operator = functionExpression.getOperator();
            var result = functionExpression.getTerms().stream().map(term -> term.accept(this));


            return new JsonFunctionDto(operator.getKey(), result);
        }

        @Override
        public JsonExpressionDto visit(SymbolicReference ref) {
            var jsonExprTerms = ref.getPath().stream().map(p -> p.accept(this)).collect(Collectors.toList());
            return new JsonSymbolicReferenceDto(ref.getSubject().accept(this), jsonExprTerms);
        }

        @Override
        public JsonExpressionDto visit(Variable variable) {
            return new JsonVariableDto(variable.getName());
        }


    }


}
