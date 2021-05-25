package eu.xenit.contentcloud.security.pbac.predicates.converters.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.xenit.contentcloud.security.pbac.predicates.model.Expression;
import eu.xenit.contentcloud.security.pbac.predicates.model.ExpressionVisitor;
import eu.xenit.contentcloud.security.pbac.predicates.model.FunctionExpression;
import eu.xenit.contentcloud.security.pbac.predicates.model.Scalar;
import eu.xenit.contentcloud.security.pbac.predicates.model.SymbolicReference;
import eu.xenit.contentcloud.security.pbac.predicates.model.Variable;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

public class ExpressionJsonConverter {

    private final ObjectMapper objectMapper;
    private final JsonConverterVisitor visitor = new JsonConverterVisitor();

    public ExpressionJsonConverter() {
        this(new ObjectMapper());
    }

    public ExpressionJsonConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String toJson(Expression<?> expression) {
        try {
            JsonExpressionDto jsonDto = expression.accept(this.visitor);
            return this.objectMapper.writeValueAsString(jsonDto);
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static class JsonConverterVisitor implements ExpressionVisitor<JsonExpressionDto> {

        final Map<Class<?>, String> supportedScalarTypes = Map.of(
                String.class, "string",
                Number.class, "number",
                Boolean.class, "bool",
                Void.class, "null");

        @Override
        public JsonExpressionDto visit(Scalar<?> scalar) {
            var resultType = scalar.getResultType();

            var typeName = supportedScalarTypes.get(resultType);
            if (typeName == null) {
                String exMessage = String.format("Scalar of type <%s> is not supported", resultType.getName());
                throw new IllegalArgumentException(exMessage);
            }

            return new JsonScalarDto(typeName, scalar.getValue());
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


    private static class JsonExpressionDto {

    }

    @Data
    @EqualsAndHashCode(callSuper = false)
    private static class JsonScalarDto<T> extends JsonExpressionDto {

        private String type;
        private T value;

        private JsonScalarDto(String type, T value) {
            this.type = type;
            this.value = value;
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = false)
    private static class JsonFunctionDto extends JsonExpressionDto {

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
    }

    @Data
    @EqualsAndHashCode(callSuper = false)
    @AllArgsConstructor
    private static class JsonVariableDto extends JsonExpressionDto {

        private final String type = "var";
        private String name;

    }

    @Data
    @EqualsAndHashCode(callSuper = false)
    private static class JsonSymbolicReferenceDto extends JsonExpressionDto {

        private final String type = "ref";
        private JsonExpressionDto subject;
        private List<JsonExpressionDto> path;

        public JsonSymbolicReferenceDto(JsonExpressionDto subject, List<JsonExpressionDto> path) {
            super();
            this.subject = subject;
            this.path = path;
        }
    }
}
