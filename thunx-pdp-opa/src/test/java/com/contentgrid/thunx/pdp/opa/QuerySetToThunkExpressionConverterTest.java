package com.contentgrid.thunx.pdp.opa;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import com.contentgrid.opa.rego.ast.Expression;
import com.contentgrid.opa.rego.ast.Query;
import com.contentgrid.opa.rego.ast.QuerySet;
import com.contentgrid.opa.rego.ast.Term;
import com.contentgrid.thunx.predicates.model.CollectionValue;
import com.contentgrid.thunx.predicates.model.Comparison;
import com.contentgrid.thunx.predicates.model.LogicalOperation;
import com.contentgrid.thunx.predicates.model.Scalar;
import com.contentgrid.thunx.predicates.model.SetValue;
import com.contentgrid.thunx.predicates.model.SymbolicReference;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class QuerySetToThunkExpressionConverterTest {

    private final QuerySetToThunkExpressionConverter converter = new QuerySetToThunkExpressionConverter();

    @Nested
    class TestOperators {

        @Test
        void equals() {
            // input.entity.color == "blue"
            var opaExpr = new Expression(0, List.of(
                    new Term.Ref(List.of(new Term.Var("eq"))),
                    new Term.Ref(List.of(
                            new Term.Var("input"),
                            new Term.Text("entity"),
                            new Term.Text("color")
                    )),
                    new Term.Text("blue")
            ));

            assertThat(converter.convert(opaExpr)).isEqualTo(Comparison.areEqual(
                    SymbolicReference.of("entity", path -> path.string("color")),
                    Scalar.of("blue")
            ));
        }

        @Test
        void not_equals() {
            // input.entity.color != "blue"
            var opaExpr = new Expression(0, List.of(
                    new Term.Ref(List.of(new Term.Var("neq"))),
                    new Term.Ref(List.of(
                            new Term.Var("input"),
                            new Term.Text("entity"),
                            new Term.Text("color")
                    )),
                    new Term.Text("blue")
            ));

            assertThat(converter.convert(opaExpr)).isEqualTo(Comparison.notEqual(
                    SymbolicReference.of("entity", path -> path.string("color")),
                    Scalar.of("blue")
            ));
        }

        @Test
        void greater_than() {
            // input.entity.security > 5
            var opaExpr = new Expression(0, List.of(
                    new Term.Ref(List.of(new Term.Var("gt"))),
                    new Term.Ref(List.of(
                            new Term.Var("input"),
                            new Term.Text("entity"),
                            new Term.Text("security")
                    )),
                    new Term.Numeric(5)
            ));

            assertThat(converter.convert(opaExpr)).isEqualTo(Comparison.greater(
                    SymbolicReference.of("entity", path -> path.string("security")),
                    Scalar.of(5)
            ));
        }

        @Test
        void greater_or_equals() {
            // input.entity.security >= 5
            var opaExpr = new Expression(0, List.of(
                    new Term.Ref(List.of(new Term.Var("gte"))),
                    new Term.Ref(List.of(
                            new Term.Var("input"),
                            new Term.Text("entity"),
                            new Term.Text("security")
                    )),
                    new Term.Numeric(5)
            ));

            assertThat(converter.convert(opaExpr)).isEqualTo(Comparison.greaterOrEquals(
                    SymbolicReference.of("entity", path -> path.string("security")),
                    Scalar.of(5)
            ));
        }

        @Test
        void less_than() {
            // input.entity.security < 5
            var opaExpr = new Expression(0, List.of(
                    new Term.Ref(List.of(new Term.Var("lt"))),
                    new Term.Ref(List.of(
                            new Term.Var("input"),
                            new Term.Text("entity"),
                            new Term.Text("security")
                    )),
                    new Term.Numeric(5)
            ));

            assertThat(converter.convert(opaExpr)).isEqualTo(Comparison.less(
                    SymbolicReference.of("entity", path -> path.string("security")),
                    Scalar.of(5)
            ));
        }

        @Test
        void less_or_equals() {
            // input.entity.security <= 5
            var opaExpr = new Expression(0, List.of(
                    new Term.Ref(List.of(new Term.Var("lte"))),
                    new Term.Ref(List.of(
                            new Term.Var("input"),
                            new Term.Text("entity"),
                            new Term.Text("security")
                    )),
                    new Term.Numeric(5)
            ));

            assertThat(converter.convert(opaExpr)).isEqualTo(Comparison.lessOrEquals(
                    SymbolicReference.of("entity", path -> path.string("security")),
                    Scalar.of(5)
            ));
        }

        @Test
        void in() {
            // input.entity.security in {4, 5}
            var opaExpr = new Expression(0, List.of(
                    new Term.Ref(List.of(new Term.Var("internal"), new Term.Text("member_2"))),
                    new Term.Ref(List.of(
                            new Term.Var("input"),
                            new Term.Text("entity"),
                            new Term.Text("security")
                    )),
                    new Term.SetTerm(Set.of(new Term.Numeric(4), new Term.Numeric(5)))
            ));

            assertThat(converter.convert(opaExpr)).isEqualTo(Comparison.in(
                    SymbolicReference.of("entity", path -> path.string("security")),
                    new SetValue(Set.of(Scalar.of(4), Scalar.of(5)))
            ));
        }

    }

    @Test
    void querySetNull_meansAccessDenied() {
        var result = converter.convert((QuerySet) null);

        assertThat(result).isNotNull().isEqualTo(Scalar.of(false));
    }

    @Test
    void querySetWithEmptyQueryArray_meansAccessGranted() {
        var querySet = new QuerySet(new Query());
        var result = new QuerySetToThunkExpressionConverter().convert(querySet);

        assertThat(result).isNotNull().isEqualTo(Scalar.of(true));
    }

    @Test
    void opaExpression_singleQuery_singleExpr() {
        //     infix:   4 >= data.reports[_].clearance_lavel
        // expr call:   gte ( 4, data.reports[_].clearance_level)
        var opaExpr = new Expression(0, List.of(
                new Term.Ref(List.of(new Term.Var("gte"))),
                new Term.Numeric(4),
                new Term.Ref(List.of(
                        new Term.Var("data"),
                        new Term.Text("reports"),
                        new Term.Var("$01"),
                        new Term.Text("clearance_level")
                ))
        ));

        var expr = new QuerySetToThunkExpressionConverter().convert(opaExpr);
        assertThat(expr).isNotNull()
                .isInstanceOf(Comparison.class)
                .isEqualTo(Comparison.greaterOrEquals(
                        Scalar.of(4),
                        SymbolicReference.of("data", p -> p.string("reports").var("$01").string("clearance_level"))));
    }

    @Test
    void opaExpression_collection_in_collection() {
        // input.entity.security in {4, 5, {6, 7}}
        var opaExpr = new Expression(0, List.of(
                new Term.Ref(List.of(new Term.Var("internal"), new Term.Text("member_2"))),
                new Term.Ref(List.of(
                        new Term.Var("input"),
                        new Term.Text("entity"),
                        new Term.Text("security")
                )),
                new Term.SetTerm(Set.of(new Term.Numeric(4), new Term.Numeric(5),
                        new Term.SetTerm(Set.of(new Term.Numeric(6), new Term.Numeric(7)))
                ))));

        assertThat(converter.convert(opaExpr)).isEqualTo(Comparison.in(
                SymbolicReference.of("entity", path -> path.string("security")),
                new SetValue(Set.of(Scalar.of(4), Scalar.of(5),
                        new SetValue(Set.of(Scalar.of(6), Scalar.of(7))))
                )));
    }

    /**
     * Scenario for partial evaluation of a policy, based on 'group' attribute on a set of documents/results
     *
     * Request: GET /documents
     *
     * Context: 1. A user is member of a set of groups, expressed via an array as `input.user.groups[]` 2. Documents
     * have a (single) `group` attribute. Request: GET /documents
     *
     * Policy: A user can access a document, when the document has a `group` attribute that matches with a group from
     * his users' profile.
     *
     * Applied: user is member of `group-a` and `group-b`.
     */
    @Test
    void partialEval_multiQuery_singleExpr() {
        // input.user.groups = [ "group-a", "group-b" ]
        // input.entity.group IS IN [ "group-a", "group-b" ]
        //
        // OPA partial eval turns this into a disjunction:
        // input.entity.group == "group-a" OR input.entity.group == "group-b"
        var opaQuerySet = new QuerySet(
                new Query(new Expression(0, List.of(
                        new Term.Ref(List.of(new Term.Var("eq"))),
                        new Term.Text("group-a"),
                        new Term.Ref(List.of(
                                new Term.Var("input"),
                                new Term.Text("entity"),
                                new Term.Text("group")))

                ))),
                new Query(new Expression(0, List.of(
                        new Term.Ref(List.of(new Term.Var("eq"))),
                        new Term.Text("group-b"),
                        new Term.Ref(List.of(
                                new Term.Var("input"),
                                new Term.Text("entity"),
                                new Term.Text("group")))
                ))));

        var expr = new QuerySetToThunkExpressionConverter().convert(opaQuerySet);
        assertThat(expr).isNotNull()
                .isInstanceOf(LogicalOperation.class)
                .isEqualTo(LogicalOperation.disjunction(List.of(
                        Comparison.areEqual(
                                Scalar.of("group-a"),
                                SymbolicReference
                                        .of("entity", path -> path.string("group"))),
                        Comparison.areEqual(
                                Scalar.of("group-b"),
                                SymbolicReference
                                        .of("entity", path -> path.string("group")))
                )));

    }

    @Test
    void optimizeExpressionWithSingleTerm() {
        // expressions should be a conjunction of all their terms
        // but if there is a single-term expression, it should be unwrapped
        var opaQuery = new Query(
                new Expression(0,
                        new Term.Ref(List.of(new Term.Var("eq"))),
                        new Term.Text("group-a"),
                        new Term.Ref(List.of(
                                new Term.Var("data"),
                                new Term.Text("documents"),
                                new Term.Var("$11"),
                                new Term.Text("group")))));

        var expr = new QuerySetToThunkExpressionConverter().convert(opaQuery);

        assertThat(expr)
                .isNotInstanceOf(LogicalOperation.class)
                .isInstanceOf(Comparison.class)
                .isEqualTo(Comparison.areEqual(
                        Scalar.of("group-a"),
                        SymbolicReference.of("data", path -> path.string("documents").var("$11").string("group"))
                ));
    }
}