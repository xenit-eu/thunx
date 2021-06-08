package eu.contentcloud.security.abac.pdp.opa;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import eu.contentcloud.abac.opa.rego.ast.Query;
import eu.contentcloud.abac.opa.rego.ast.QuerySet;
import eu.contentcloud.abac.opa.rego.ast.Term.Ref;
import eu.contentcloud.abac.predicates.model.Comparison;
import eu.contentcloud.abac.opa.rego.ast.Expression;
import eu.contentcloud.abac.opa.rego.ast.Term;
import eu.contentcloud.abac.opa.rego.ast.Term.Numeric;
import eu.contentcloud.abac.opa.rego.ast.Term.Text;
import eu.contentcloud.abac.predicates.model.FunctionExpression.Operator;
import eu.contentcloud.abac.predicates.model.LogicalOperation;
import eu.contentcloud.abac.predicates.model.Scalar;
import eu.contentcloud.abac.predicates.model.SymbolicReference;
import java.math.BigDecimal;
import java.util.List;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.AssertFactory;
import org.assertj.core.api.AssertionsForClassTypes;
import org.assertj.core.api.InstanceOfAssertFactory;
import org.junit.jupiter.api.Test;

class QuerySetToThunkExpressionConverterTest {

    @Test
    void opaExpression_singleQuery_singleExpr() {
        //     infix:   4 >= data.reports[_].clearance_lavel
        // expr call:   gte ( 4, data.reports[_].clearance_level)
        var opaExpr = new Expression(0, List.of(
                new Term.Ref(List.of(new Term.Var("gte"))),
                new Numeric(4),
                new Term.Ref(List.of(
                        new Term.Var("data"),
                        new Text("reports"),
                        new Term.Var("$01"),
                        new Text("clearance_level")
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
        // data.documents[_].group IS IN [ "group-a", "group-b" ]
        // OPA partial eval turns this into a disjunction:
        // data.documents[_].group == "group-a" OR data.documents[_].group == "group-b"
        var opaQuerySet = new QuerySet(
                new Query(new Expression(0, List.of(
                        new Ref(List.of(new Term.Var("eq"))),
                        new Term.Text("group-a"),
                        new Ref(List.of(
                                new Term.Var("data"),
                                new Term.Text("documents"),
                                new Term.Var("$11"),
                                new Term.Text("group")))

                ))), new Query(new Expression(0, List.of(
                new Ref(List.of(new Term.Var("eq"))),
                new Term.Text("group-b"),
                new Ref(List.of(
                        new Term.Var("data"),
                        new Term.Text("documents"),
                        new Term.Var("$11"),
                        new Term.Text("group")))
        ))));

        var expr = new QuerySetToThunkExpressionConverter().convert(opaQuerySet);
        assertThat(expr).isNotNull()
                .isInstanceOf(LogicalOperation.class)
                .isEqualTo(LogicalOperation.disjunction(List.of(
                        Comparison.areEqual(
                                Scalar.of("group-a"),
                                SymbolicReference
                                        .of("data", path -> path.string("documents").var("$11").string("group"))),
                        Comparison.areEqual(
                                Scalar.of("group-b"),
                                SymbolicReference
                                        .of("data", path -> path.string("documents").var("$11").string("group"))
                                ))));

    }

    @Test
    void optimizeExpressionWithSingleTerm() {
        // expressions should be a conjunction of all their terms
        // but if there is a single-term expression, it should be unwrapped
        var opaQuery = new Query(
                new Expression(0,
                        new Ref(List.of(new Term.Var("eq"))),
                        new Term.Text("group-a"),
                        new Ref(List.of(
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