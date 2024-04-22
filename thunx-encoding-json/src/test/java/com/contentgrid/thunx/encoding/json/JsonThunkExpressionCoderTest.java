package com.contentgrid.thunx.encoding.json;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.contentgrid.thunx.encoding.json.InvalidExpressionDataException.InvalidExpressionValueException;
import com.contentgrid.thunx.predicates.model.Comparison;
import com.contentgrid.thunx.predicates.model.LogicalOperation;
import com.contentgrid.thunx.predicates.model.Scalar;
import com.contentgrid.thunx.predicates.model.SymbolicReference;
import com.contentgrid.thunx.predicates.model.Variable;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class JsonThunkExpressionCoderTest {

    private final JsonThunkExpressionCoder converter = new JsonThunkExpressionCoder();

    @Nested
    class Encoder {

        @Nested
        class Scalars {

            @Test
            void string_toJson() {
                var expr = Scalar.of("foobar");
                var result = converter.encodeToJson(expr);

                assertThatJson(result).isEqualTo("{ type: 'string', value: 'foobar' }");
            }

            @Test
            void intNumber_toJson() {
                var result = converter.encodeToJson(Scalar.of(42));
                assertThatJson(result).isEqualTo("{ type: 'number', value: 42 }");
            }

            @Test
            void doubleNumber_toJson() {
                var expr = Scalar.of(Math.PI);
                var result = converter.encodeToJson(expr);

                assertThatJson(result).isEqualTo("{ type: 'number', value: 3.141592653589793 }");
            }

            @Test
            void boolean_toJson() {
                var jsonExprTrue = converter.encodeToJson(Scalar.of(true));
                assertThatJson(jsonExprTrue).isEqualTo("{ type: 'bool', value: true }");

                var jsonExprFalse = converter.encodeToJson(Scalar.of(false));
                assertThatJson(jsonExprFalse).isEqualTo("{ type: 'bool', value: false }");
            }

            @Test
            void null_toJson() {
                var result = converter.encodeToJson(Scalar.nullValue());

                // Map.of(...) does not support null-values :facepalm:
                assertThatJson(result).isEqualTo("{ type: 'null' }");
            }
        }

        @Nested
        class Operators {

            @Test
            void is_equal() {
                // answer == 42
                var expr = Comparison.areEqual(Variable.named("answer"), Scalar.of(42));
                var result = converter.encodeToJson(expr);

                assertThatJson(result)
                        .isEqualTo("{ type: 'function', operator: 'eq', terms: ["
                                + "     { type: 'var', name: 'answer' },"
                                + "     { type: 'number', value: 42 }"
                                + "]}");
            }

            @Test
            void is_not_equal() {
                // answer != 42
                var expr = Comparison.notEqual(Variable.named("answer"), Scalar.of(42));
                var result = converter.encodeToJson(expr);

                assertThatJson(result)
                        .isEqualTo("{ type: 'function', operator: 'neq', terms: ["
                                + "     { type: 'var', name: 'answer' },"
                                + "     { type: 'number', value: 42 }"
                                + "]}");
            }

            @Test
            void is_greater_than() {
                // answer > 42
                var expr = Comparison.greater(Variable.named("answer"), Scalar.of(42));
                var result = converter.encodeToJson(expr);

                assertThatJson(result)
                        .isEqualTo("{ type: 'function', operator: 'gt', terms: ["
                                + "     { type: 'var', name: 'answer' },"
                                + "     { type: 'number', value: 42 }"
                                + "]}");
            }

            @Test
            void is_greater_or_equals() {
                // answer >= 42
                var expr = Comparison.greaterOrEquals(Variable.named("answer"), Scalar.of(42));
                var result = converter.encodeToJson(expr);

                assertThatJson(result)
                        .isEqualTo("{ type: 'function', operator: 'gte', terms: ["
                                + "     { type: 'var', name: 'answer' },"
                                + "     { type: 'number', value: 42 }"
                                + "]}");
            }

            @Test
            void is_less_than() {
                // answer < 42
                var expr = Comparison.less(Variable.named("answer"), Scalar.of(42));
                var result = converter.encodeToJson(expr);

                assertThatJson(result)
                        .isEqualTo("{ type: 'function', operator: 'lt', terms: ["
                                + "     { type: 'var', name: 'answer' },"
                                + "     { type: 'number', value: 42 }"
                                + "]}");
            }

            @Test
            void is_less_or_equals() {
                // answer <= 42
                var expr = Comparison.lessOrEquals(Variable.named("answer"), Scalar.of(42));
                var result = converter.encodeToJson(expr);

                assertThatJson(result)
                        .isEqualTo("{ type: 'function', operator: 'lte', terms: ["
                                + "     { type: 'var', name: 'answer' },"
                                + "     { type: 'number', value: 42 }"
                                + "]}");
            }

            @Test
            void logical_disjunction() {
                // rules: answer == 42 OR user.admin == true
                var rule1 = Comparison.areEqual(Variable.named("answer"), Scalar.of(42));
                var rule2 = Comparison.areEqual(SymbolicReference.of("user.admin"), Scalar.of(true));
                var disjunction = LogicalOperation.disjunction(rule1, rule2);

                var result = converter.encodeToJson(disjunction);

                assertThatJson(result)
                        .isEqualTo("{ type: 'function', operator: 'or', terms: ["
                                + "     { type: 'function', operator: 'eq', terms: [{ type: 'var', name: 'answer' }, { type: 'number', value: 42 } ]},"
                                + "     { type: 'function', operator: 'eq', terms: [{ type: 'ref', path: [], subject: { type: 'var', name: 'user.admin' }}, { type: 'bool', value: true }] }"
                                + "]}");
            }

            @Test
            void logical_conjunction() {
                // rules: answer == 42 AND user.admin == true
                var rule1 = Comparison.areEqual(Variable.named("answer"), Scalar.of(42));
                var rule2 = Comparison.areEqual(SymbolicReference.of("user.admin"), Scalar.of(true));
                var disjunction = LogicalOperation.conjunction(rule1, rule2);

                var result = converter.encodeToJson(disjunction);

                assertThatJson(result)
                        .isEqualTo("{ type: 'function', operator: 'and', terms: ["
                                + "     { type: 'function', operator: 'eq', terms: [{ type: 'var', name: 'answer' }, { type: 'number', value: 42 } ]},"
                                + "     { type: 'function', operator: 'eq', terms: [{ type: 'ref', path: [], subject: { type: 'var', name: 'user.admin' }}, { type: 'bool', value: true }] }"
                                + "]}");
            }

            @Test
            void logical_negation() throws InvalidExpressionDataException, JsonProcessingException {
                // not(answer)
                var expression = LogicalOperation.uncheckedNegation(List.of(Variable.named("answer")));
                var json = converter.encodeToJson(expression);

                assertThatJson(json)
                        .isEqualTo("{ type: 'function', operator: 'not', terms: ["
                                + "     { type: 'var', name: 'answer' }"
                                + "]}");
            }

        }

        @Nested
        class Variables {

            @Test
            void variable() {
                var expr = Variable.named("foo");
                var result = converter.encodeToJson(expr);

                assertThatJson(result).isEqualTo("{ type: 'var', name: 'foo' }");
            }
        }

        @Nested
        class SymbolicReferences {

            @Test
            void references() {
                // answers.life
                var expr = SymbolicReference.of("answers", path -> path.string("life"));
                var result = converter.encodeToJson(expr);

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
                var json = mapper.valueToTree(Map.of(
                        "type", "string",
                        "value", "foobar"
                ));

                var actual = converter.decodeFromJson(json);
                assertThat(actual).isEqualTo(Scalar.of("foobar"));
            }

            @Test
            void string_invalidValue() {
                var json = mapper.valueToTree(Map.of(
                        "type", "string",
                        "value", true
                ));

                assertThatThrownBy(() -> converter.decodeFromJson(json))
                        .isInstanceOf(InvalidExpressionValueException.class);
            }

            @Test
            void string_nullValue() throws JsonProcessingException {
                var json = mapper.readTree("{ \"type\": \"string\", \"value\": null }");

                assertThatThrownBy(() -> converter.decodeFromJson(json))
                        .isInstanceOf(InvalidExpressionValueException.class);
            }

            @Test
            void number_intValue() throws JsonProcessingException, InvalidExpressionDataException {
                var json = mapper.valueToTree(Map.of(
                        "type", "number",
                        "value", 42
                ));

                var actual = converter.decodeFromJson(json);
                assertThat(actual).isEqualTo(Scalar.of(42));
            }

            @Test
            void number_doubleValue() throws JsonProcessingException, InvalidExpressionDataException {
                var json = mapper.valueToTree(Map.of(
                        "type", "number",
                        "value", Math.PI
                ));

                var actual = converter.decodeFromJson(json);
                assertThat(actual).isEqualTo(Scalar.of(3.141592653589793));
            }

            @Test
            void number_nullValue_shouldFail() throws JsonProcessingException {
                var json = mapper.readTree("{ \"type\": \"number\", \"value\": null }");

                assertThatThrownBy(() -> converter.decodeFromJson(json))
                        .isInstanceOf(InvalidExpressionValueException.class);
            }

            @Test
            void number_stringValue_shouldFail() throws JsonProcessingException {
                var json = mapper.readTree("{ \"type\": \"number\", \"value\": \"5\" }");

                assertThatThrownBy(() -> converter.decodeFromJson(json))
                        .isInstanceOf(InvalidExpressionValueException.class);
            }

            @Test
            void booleanValue_true() throws JsonProcessingException, InvalidExpressionDataException {
                var json = mapper.valueToTree(Map.of(
                        "type", "bool",
                        "value", true
                ));
                var actual = converter.decodeFromJson(json);
                assertThat(actual).isEqualTo(Scalar.of(true));
            }

            @Test
            void booleanValue_false() throws JsonProcessingException, InvalidExpressionDataException {
                var json = mapper.valueToTree(Map.of(
                        "type", "bool",
                        "value", false
                ));
                var actual = converter.decodeFromJson(json);
                assertThat(actual).isEqualTo(Scalar.of(false));
            }

            @Test
            void nullValue() throws JsonProcessingException, InvalidExpressionDataException {
                var json = mapper.valueToTree(Map.of(
                        "type", "null"
                ));
                var actual = converter.decodeFromJson(json);
                assertThat(actual).isEqualTo(Scalar.nullValue());
            }
        }

        @Nested
        class Variables {

            @Test
            void variable() throws JsonProcessingException {
                var json = mapper.readTree("{ \"type\": \"var\", \"name\": \"foo\" }");
                var actual = converter.decodeFromJson(json);

                assertThat(actual).isEqualTo(Variable.named("foo"));
            }
        }

        @Nested
        class SymbolicReferences {

            @Test
            void references() throws JsonProcessingException {
                // answers.life
                var json = mapper.valueToTree(Map.of(
                    "type", "ref",
                        "subject", Map.of("type", "var", "name", "answers"),
                        "path", List.of(Map.of("type", "string", "value", "life"))
                ));
                var actual = converter.decodeFromJson(json);

                assertThatJson(actual)
                        .isEqualTo(SymbolicReference.of("answers", path -> path.string("life")));
            }
        }

        @Nested
        class Operators {

            @Test
            void is_equal() throws InvalidExpressionDataException {
                // answer == 42
                var json = converter.encode(Comparison.areEqual(Variable.named("answer"), Scalar.of(42)));
                var expr = converter.decode(json);

                assertThat(expr).isEqualTo(Comparison.areEqual(Variable.named("answer"), Scalar.of(42)));
            }

            @Test
            void is_not_equal() throws InvalidExpressionDataException {
                // answer != 42
                var json = converter.encode(Comparison.notEqual(Variable.named("answer"), Scalar.of(42)));
                var expr = converter.decode(json);

                assertThat(expr).isEqualTo(Comparison.notEqual(Variable.named("answer"), Scalar.of(42)));
            }

            @Test
            void is_greater_than() throws InvalidExpressionDataException {
                // answer > 42
                var json = converter.encode(Comparison.greater(Variable.named("answer"), Scalar.of(42)));
                var expr = converter.decode(json);

                assertThat(expr).isEqualTo(Comparison.greater(Variable.named("answer"), Scalar.of(42)));
            }

            @Test
            void is_greater_or_equals() throws InvalidExpressionDataException {
                // answer >= 42
                var json = converter.encode(Comparison.greaterOrEquals(Variable.named("answer"), Scalar.of(42)));
                var expr = converter.decode(json);

                assertThat(expr).isEqualTo(Comparison.greaterOrEquals(Variable.named("answer"), Scalar.of(42)));
            }

            @Test
            void is_less_than() throws InvalidExpressionDataException {
                // answer < 42
                var json = converter.encode(Comparison.less(Variable.named("answer"), Scalar.of(42)));
                var expr = converter.decode(json);

                assertThat(expr).isEqualTo(Comparison.less(Variable.named("answer"), Scalar.of(42)));
            }

            @Test
            void is_less_or_equals() throws InvalidExpressionDataException {
                // answer <= 42
                var json = converter.encode(Comparison.lessOrEquals(Variable.named("answer"), Scalar.of(42)));
                var expr = converter.decode(json);

                assertThat(expr).isEqualTo(Comparison.lessOrEquals(Variable.named("answer"), Scalar.of(42)));
            }

            @Test
            void logical_disjunction() {
                // rules: answer == 42 OR user.admin == true
                var rule1 = Comparison.areEqual(Variable.named("answer"), Scalar.of(42));
                var rule2 = Comparison.areEqual(SymbolicReference.of("user.admin"), Scalar.of(true));
                var disjunction = LogicalOperation.disjunction(rule1, rule2);

                var json = converter.encode(disjunction);
                var expr = converter.decode(json);

                assertThatJson(expr).isEqualTo(disjunction);
            }

            @Test
            void logical_conjunction() {
                // rules: answer == 42 AND user.admin == true
                var rule1 = Comparison.areEqual(Variable.named("answer"), Scalar.of(42));
                var rule2 = Comparison.areEqual(SymbolicReference.of("user.admin"), Scalar.of(true));
                var conjunction = LogicalOperation.conjunction(rule1, rule2);

                var json = converter.encode(conjunction);
                var expr = converter.decode(json);

                assertThatJson(expr).isEqualTo(conjunction);
            }

            @Test
            void logical_negation() throws InvalidExpressionDataException {
                // not(answer)
                var expression = LogicalOperation.uncheckedNegation(List.of(Variable.named("answer")));
                var json = converter.encode(expression);

                assertThat(converter.decode(json)).isEqualTo(expression);
            }
        }


    }

}