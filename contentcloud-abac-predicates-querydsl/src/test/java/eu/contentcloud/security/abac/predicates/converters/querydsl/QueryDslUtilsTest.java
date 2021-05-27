package eu.contentcloud.security.abac.predicates.converters.querydsl;

import static org.assertj.core.api.Assertions.assertThat;

import com.querydsl.core.types.Ops;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.PathBuilder;
import eu.contentcloud.abac.predicates.model.Comparison;
import eu.contentcloud.abac.predicates.model.Scalar;
import eu.contentcloud.abac.predicates.model.SymbolicReference;
import lombok.Data;
import org.junit.jupiter.api.Test;

class QueryDslUtilsTest {

    @Data
    static class Document {
        int security;
    }
    @Test
    void test() {
        // document.security == 5
        var abacExpr = Comparison.areEqual(
                SymbolicReference.of("document", builder -> builder.path("security")),
                Scalar.of(5)
        );

        var document = new PathBuilder(Document.class, "document");
//        var document = Expressions.path(Document.class, "document");

        var expected = Expressions.predicate(
                Ops.EQ,
                Expressions.path(Integer.class, document, "security"),
                Expressions.constant(5)
        );

        var actual = QueryDslUtils.from(abacExpr, document);
        assertThat(actual)
                .isNotNull()
                .hasToString(expected.toString());

    }
}