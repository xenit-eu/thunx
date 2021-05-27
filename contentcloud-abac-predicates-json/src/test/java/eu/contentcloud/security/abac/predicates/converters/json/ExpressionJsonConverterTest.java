package eu.contentcloud.security.abac.predicates.converters.json;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.contentcloud.abac.predicates.model.Comparison;
import eu.contentcloud.abac.predicates.model.SymbolicReference;
import eu.contentcloud.abac.predicates.model.Variable;
import eu.contentcloud.abac.predicates.model.Scalar;
import java.math.BigDecimal;
import java.util.Map;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ExpressionJsonConverterTest {

    private ExpressionJsonConverter converter = new ExpressionJsonConverter();

    @Nested
    class Encoder {

        @Nested
        class Scalars {

            @Test
            void string_toJson() {
                var expr = Scalar.of("foobar");
                var result = converter.encode(expr);

                assertThatJson(result).isObject()
                        .hasSize(2)
                        .containsEntry("type", "string")
                        .containsEntry("value", "foobar");
            }

            @Test
            void intNumber_toJson() {
                var result = converter.encode(Scalar.of(42));
                assertThatJson(result).isObject()
                        .hasSize(2)
                        .containsEntry("type", "number")
                        .containsEntry("value", BigDecimal.valueOf(42));
            }

            @Test
            void doubleNumber_toJson() {
                var expr = Scalar.of(Math.PI);
                var result = converter.encode(expr);

                assertThatJson(result).isObject()
                        .hasSize(2)
                        .containsEntry("type", "number")
                        .containsEntry("value", BigDecimal.valueOf(3.141592653589793D));
            }

            @Test
            void boolean_toJson() {
                String jsonExprTrue = converter.encode(Scalar.of(true));
                assertThatJson(jsonExprTrue).isObject()
                        .hasSize(2)
                        .containsEntry("type", "bool")
                        .containsEntry("value", true);

                var jsonExprFalse = converter.encode(Scalar.of(false));
                assertThatJson(jsonExprFalse).isObject()
                        .hasSize(2)
                        .containsEntry("type", "bool")
                        .containsEntry("value", false);
            }

            @Test
            void null_toJson() {
                String result = converter.encode(Scalar.nullValue());

                // Map.of(...) does not support null-values :facepalm:
                assertThatJson(result).isObject()
                        .hasSize(2)
                        .containsEntry("type", "null")
                        .containsEntry("value", null);

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
                        .isObject()
                        .hasSize(3)
                        .containsEntry("type", "function")
                        .containsEntry("operator", "eq")
                        .hasEntrySatisfying("terms", terms -> assertThatJson(terms)
                                .isArray()
                                .hasSize(2)
                                .satisfiesExactly(
                                        e -> assertThatJson(e).isObject().containsEntry("name", "answer"),
                                        e -> assertThatJson(e).isObject().containsEntry("value", BigDecimal.valueOf(42))
                                ));
            }
        }

        @Nested
        class Variables {

            @Test
            void variable() {
                var expr = Variable.named("foo");
                var result = converter.encode(expr);

                assertThatJson(result).isObject()
                        .hasSize(2)
                        .containsEntry("type", "var")
                        .containsEntry("name", "foo");
            }
        }

        @Nested
        class SymbolicReferences {

            @Test
            void references() {
                var expr = SymbolicReference
                        .of(Variable.named("answers"), SymbolicReference.path("life, universe and everything"));
                var result = converter.encode(expr);
                System.out.println(result);

                assertThatJson(result).isObject()
                        .hasSize(3)
                        .containsEntry("type", "ref")
                        .hasEntrySatisfying("subject", subject -> assertThatJson(subject)
                                .isObject()
                                .hasSize(2)
                                .containsEntry("type", "var")
                                .containsEntry("name", "answers"))
                        .hasEntrySatisfying("path", path -> assertThatJson(path)
                                .isArray()
                                .hasSize(1)
                                .satisfiesExactly(e -> assertThatJson(e)
                                        .isObject()
                                        .hasSize(2)
                                        .containsEntry("type", "string")
                                        .containsEntry("value", "life, universe and everything")));


            }
        }
    }

    @Nested
    class Decoder {

        private ObjectMapper mapper = new ObjectMapper();

        @Nested
        class Scalars {

            @Test
            void string_fromJson() throws JsonProcessingException {
                var json = mapper.writeValueAsString(Map.of(
                        "type", "string",
                        "value", "foobar"
                ));

                var actual = converter.decode(json);
                assertThat(actual).isEqualTo(Scalar.of("foobar"));
            }

            @Test
            void intNumber_toJson() {
                var result = converter.encode(Scalar.of(42));
                assertThatJson(result).isObject()
                        .hasSize(2)
                        .containsEntry("type", "number")
                        .containsEntry("value", BigDecimal.valueOf(42));
            }

            @Test
            void doubleNumber_toJson() {
                var expr = Scalar.of(Math.PI);
                var result = converter.encode(expr);

                assertThatJson(result).isObject()
                        .hasSize(2)
                        .containsEntry("type", "number")
                        .containsEntry("value", BigDecimal.valueOf(3.141592653589793D));
            }

            @Test
            void boolean_toJson() {
                String jsonExprTrue = converter.encode(Scalar.of(true));
                assertThatJson(jsonExprTrue).isObject()
                        .hasSize(2)
                        .containsEntry("type", "bool")
                        .containsEntry("value", true);

                var jsonExprFalse = converter.encode(Scalar.of(false));
                assertThatJson(jsonExprFalse).isObject()
                        .hasSize(2)
                        .containsEntry("type", "bool")
                        .containsEntry("value", false);
            }

            @Test
            void null_toJson() {
                String result = converter.encode(Scalar.nullValue());

                // Map.of(...) does not support null-values :facepalm:
                assertThatJson(result).isObject()
                        .hasSize(2)
                        .containsEntry("type", "null")
                        .containsEntry("value", null);

            }
        }
    }

}