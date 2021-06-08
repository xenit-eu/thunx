package eu.contentcloud.security.abac.predicates.converters.querydsl;

import static org.assertj.core.api.Assertions.assertThat;

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
    void resourceAttributeThunk() {
        // document.security == 5
        var thunkExpression = Comparison.areEqual(
                SymbolicReference.of("document", path -> path.string("security")),
                Scalar.of(5)
        );

        var document = new PathBuilder(Document.class, "document");

        var actual = QueryDslUtils.from(thunkExpression, document);
        assertThat(actual)
                .isNotNull()
                .hasToString("document.security = 5");

    }
}