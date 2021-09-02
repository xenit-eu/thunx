package eu.xenit.contentcloud.thunx.predicates.solr;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import eu.xenit.contentcloud.thunx.predicates.model.Comparison;
import eu.xenit.contentcloud.thunx.predicates.model.LogicalOperation;
import eu.xenit.contentcloud.thunx.predicates.model.Scalar;
import eu.xenit.contentcloud.thunx.predicates.model.SymbolicReference;
import org.junit.jupiter.api.Test;

class SolrUtilsTest {

    @Test
    void convert_comparison() {
        var thunkExpression = Comparison.areEqual(
                SymbolicReference.of("entity", path -> path.string("security")),
                Scalar.of(5)
        );

        var actual = NativeQueryUtils.from(thunkExpression);
        assertThat(actual)
                .isNotNull()
                .hasToString("security:5");

    }

    @Test
    void convert_complex_comparison() {
        var thunkExpression = Comparison.areEqual(
                SymbolicReference.of("entity", path -> {
                    path.string("a");
                    path.string("b");
                    path.string("c");
                }),
                Scalar.of(5)
        );

        var actual = NativeQueryUtils.from(thunkExpression);
        assertThat(actual)
                .isNotNull()
                .hasToString("a_b_c:5");

    }

    @Test
    void convert_disjunction() {
        var thunkExpression = LogicalOperation.disjunction(
                Comparison.areEqual(
                        SymbolicReference.of("entity", path -> path.string("security")),
                        Scalar.of(5)),
                Comparison.areEqual(
                        SymbolicReference.of("entity", path -> path.string("security")),
                        Scalar.of(10)),
                Comparison.areEqual(
                        SymbolicReference.of("entity", path -> path.string("security")),
                        Scalar.of(15))
        );

        var actual = NativeQueryUtils.from(thunkExpression);
        assertThat(actual)
                .isNotNull()
                .hasToString("security:5 OR security:10 OR security:15");

    }

    @Test
    void expectSubjectNotNamed_entity_throwsIllegalArgumentException() {
        var thunkExpression = Comparison.areEqual(
                SymbolicReference.of("document", path -> path.string("security")),
                Scalar.of(5)
        );

        assertThatThrownBy(() -> NativeQueryUtils.from(thunkExpression))
                .isInstanceOf(IllegalArgumentException.class);
    }
}