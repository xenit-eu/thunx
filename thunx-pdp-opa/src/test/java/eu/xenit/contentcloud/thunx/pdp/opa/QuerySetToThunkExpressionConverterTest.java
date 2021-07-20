package eu.xenit.contentcloud.thunx.pdp.opa;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import eu.xenit.contentcloud.opa.rego.ast.Expression;
import eu.xenit.contentcloud.opa.rego.ast.Query;
import eu.xenit.contentcloud.opa.rego.ast.QuerySet;
import eu.xenit.contentcloud.opa.rego.ast.Term;
import eu.xenit.contentcloud.thunx.predicates.model.Comparison;
import eu.xenit.contentcloud.thunx.predicates.model.LogicalOperation;
import eu.xenit.contentcloud.thunx.predicates.model.Scalar;
import eu.xenit.contentcloud.thunx.predicates.model.SymbolicReference;
import java.util.List;
import org.junit.jupiter.api.Test;

class QuerySetToThunkExpressionConverterTest {

    @Test
    void querySetNull_meansAccessDenied() {
        var result = new QuerySetToThunkExpressionConverter().convert((QuerySet) null);

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