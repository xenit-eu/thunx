package eu.contentcloud.security.abac.predicates.converters.json;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.contentcloud.abac.predicates.model.Comparison;
import eu.contentcloud.abac.predicates.model.Scalar;
import eu.contentcloud.abac.predicates.model.SymbolicReference;
import eu.contentcloud.abac.predicates.model.Variable;
import eu.contentcloud.security.abac.predicates.converters.json.InvalidExpressionDataException.InvalidExpressionValueException;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ExpressionJsonConverterTest {

    private final ExpressionJsonConverter converter = new ExpressionJsonConverter();

    @Nested
    class Encoder {

        @Nested
        class Scalars {

            @Test
            void string_toJson() {
                var expr = Scalar.of("foobar");
                var result = converter.encode(expr);

                assertThatJson(result).isEqualTo("{ type: 'string', value: 'foobar' }");
            }

            @Test
            void intNumber_toJson() {
                var result = converter.encode(Scalar.of(42));
                assertThatJson(result).isEqualTo("{ type: 'number', value: 42 }");
            }

            @Test
            void doubleNumber_toJson() {
                var expr = Scalar.of(Math.PI);
                var result = converter.encode(expr);

                assertThatJson(result).isEqualTo("{ type: 'number', value: 3.141592653589793 }");
            }

            @Test
            void boolean_toJson() {
                var jsonExprTrue = converter.encode(Scalar.of(true));
                assertThatJson(jsonExprTrue).isEqualTo("{ type: 'bool', value: true }");

                var jsonExprFalse = converter.encode(Scalar.of(false));
                assertThatJson(jsonExprFalse).isEqualTo("{ type: 'bool', value: false }");
            }

            @Test
            void null_toJson() {
                String result = converter.encode(Scalar.nullValue());

                // Map.of(...) does not support null-values :facepalm:
                assertThatJson(result).isEqualTo("{ type: 'null' }");
            }
        }

        @Nested
        class Functions {

            @Test
            void eq_toJson() {
                // answer == 42
                var expr = Comparison.areEqual(Variable.named("answer"), Scalar.of(42));
                var result = converter.encode(expr);

                assertThatJson(result)
                        .isEqualTo("{ type: 'function', operator: 'eq', terms: ["
                                + "     { type: 'var', name: 'answer' },"
                                + "     { type: 'number', value: 42 }"
                                + "]}");
            }
        }

        @Nested
        class Variables {

            @Test
            void variable() {
                var expr = Variable.named("foo");
                var result = converter.encode(expr);

                assertThatJson(result).isEqualTo("{ type: 'var', name: 'foo' }");
            }
        }

        @Nested
        class SymbolicReferences {

            @Test
            void references() {
                // answers.life
                var expr = SymbolicReference.of("answers", path -> path.string("life"));
                var result = converter.encode(expr);
                System.out.println(result);

                assertThatJson(result)
                        .isEqualTo("{ "
                                + "  type: 'ref',"
                                + "  subject: { type: 'var', name: 'answers' },"
                                + "  path: [{ type:'string', value:'life' }]"
                                + "}");
            }
        }
    }

    @Nested
    class Decoder {

        private final ObjectMapper mapper = new ObjectMapper();

        @Nested
        class Scalars {

            @Test
            void string() throws JsonProcessingException, InvalidExpressionDataException {
                var json = mapper.writeValueAsString(Map.of(
                        "type", "string",
                        "value", "foobar"
                ));

                var actual = converter.decode(json);
                assertThat(actual).isEqualTo(Scalar.of("foobar"));
            }

            @Test
            void string_invalidValue() throws JsonProcessingException {
                var json = mapper.writeValueAsString(Map.of(
                        "type", "string",
                        "value", true
                ));

                assertThatThrownBy(() -> converter.decode(json))
                        .isInstanceOf(InvalidExpressionValueException.class);
            }

            @Test
            void string_nullValue() {
                var json = "{ \"type\": \"string\", \"value\": null }";

                assertThatThrownBy(() -> converter.decode(json))
                        .isInstanceOf(InvalidExpressionValueException.class);
            }

            @Test
            void number_intValue() throws JsonProcessingException, InvalidExpressionDataException {
                var json = mapper.writeValueAsString(Map.of(
                        "type", "number",
                        "value", 42
                ));

                var actual = converter.decode(json);
                assertThat(actual).isEqualTo(Scalar.of(42));
            }

            @Test
            void number_doubleValue() throws JsonProcessingException, InvalidExpressionDataException {
                var json = mapper.writeValueAsString(Map.of(
                        "type", "number",
                        "value", Math.PI
                ));

                var actual = converter.decode(json);
                assertThat(actual).isEqualTo(Scalar.of(3.141592653589793));
            }

            @Test
            void number_nullValue_shouldFail() {
                var json = "{ \"type\": \"number\", \"value\": null }";

                assertThatThrownBy(() -> converter.decode(json))
                        .isInstanceOf(InvalidExpressionValueException.class);
            }

            @Test
            void number_stringValue_shouldFail() {
                var json = "{ \"type\": \"number\", \"value\": \"5\" }";

                assertThatThrownBy(() -> converter.decode(json))
                        .isInstanceOf(InvalidExpressionValueException.class);
            }

            @Test
            void booleanValue_true() throws JsonProcessingException, InvalidExpressionDataException {
                var json = mapper.writeValueAsString(Map.of(
                        "type", "bool",
                        "value", true
                ));
                var actual = converter.decode(json);
                assertThat(actual).isEqualTo(Scalar.of(true));
            }

            @Test
            void booleanValue_false() throws JsonProcessingException, InvalidExpressionDataException {
                var json = mapper.writeValueAsString(Map.of(
                        "type", "bool",
                        "value", false
                ));
                var actual = converter.decode(json);
                assertThat(actual).isEqualTo(Scalar.of(false));
            }

            @Test
            void nullValue() throws JsonProcessingException, InvalidExpressionDataException {
                var json = mapper.writeValueAsString(Map.of(
                        "type", "null"
                ));
                var actual = converter.decode(json);
                assertThat(actual).isEqualTo(Scalar.nullValue());
            }
        }

        @Nested
        class Variables {

            @Test
            void variable() throws JsonProcessingException {
                var json = "{ \"type\": \"var\", \"name\": \"foo\" }";
                var actual = converter.decode(json);

                assertThat(actual).isEqualTo(Variable.named("foo"));
            }
        }

        @Nested
        class SymbolicReferences {

            @Test
            void references() throws JsonProcessingException {
                // answers.life
                var json = mapper.writeValueAsString(Map.of(
                    "type", "ref",
                        "subject", Map.of("type", "var", "name", "answers"),
                        "path", List.of(Map.of("type", "string", "value", "life"))
                ));
                var actual = converter.decode(json);

                assertThatJson(actual)
                        .isEqualTo(SymbolicReference.of("answers", path -> path.string("life")));
            }
        }

        @Nested
        class Functions {

            @Test
            void eq() throws InvalidExpressionDataException, JsonProcessingException {
                // answer == 42
                var json = converter.encode(Comparison.areEqual(Variable.named("answer"), Scalar.of(42)));
                var expr = converter.decode(json);

                assertThat(expr).isEqualTo(Comparison.areEqual(Variable.named("answer"), Scalar.of(42)));
            }
        }


    }

}