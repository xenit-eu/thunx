package eu.xenit.contentcloud.thunx.predicates.converters.querydsl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.querydsl.core.types.dsl.PathBuilder;
import eu.xenit.contentcloud.thunx.predicates.model.Comparison;
import eu.xenit.contentcloud.thunx.predicates.model.LogicalOperation;
import eu.xenit.contentcloud.thunx.predicates.model.Scalar;
import eu.xenit.contentcloud.thunx.predicates.model.SymbolicReference;
import lombok.Data;
import org.junit.jupiter.api.Test;

class QueryDslUtilsTest {

    @Data
    static class Document {

        int security;
    }

    @Test
    void convert_comparison() {
        // document.security == 5
        var thunkExpression = Comparison.areEqual(
                SymbolicReference.of("entity", path -> path.string("security")),
                Scalar.of(5)
        );

        var document = new PathBuilder(Document.class, "document");

        var actual = QueryDslUtils.from(thunkExpression, document);
        assertThat(actual)
                .isNotNull()
                .hasToString("document.security = 5");

    }

    @Test
    void convert_disjunction() {
        // document.security == 5 OR document.security == 10
        var thunkExpression = LogicalOperation.disjunction(
                Comparison.areEqual(
                        SymbolicReference.of("entity", path -> path.string("security")),
                        Scalar.of(5)),
                Comparison.areEqual(
                        SymbolicReference.of("entity", path -> path.string("security")),
                        Scalar.of(10))
        );

        var document = new PathBuilder(Document.class, "document");

        var actual = QueryDslUtils.from(thunkExpression, document);
        assertThat(actual)
                .isNotNull()
                .hasToString("document.security = 5 || document.security = 10");

    }

    @Test
    void expectSubjectNotNamed_entity_throwsIllegalArgumentException() {
        var thunkExpression = Comparison.areEqual(
                SymbolicReference.of("document", path -> path.string("security")),
                Scalar.of(5)
        );

        var document = new PathBuilder(Document.class, "document");

        assertThatThrownBy(() -> QueryDslUtils.from(thunkExpression, document))
                .isInstanceOf(IllegalArgumentException.class);
    }
}