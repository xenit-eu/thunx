package eu.contentcloud.security.abac.pdp.opa;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import eu.contentcloud.abac.predicates.model.Comparison;
import eu.contentcloud.abac.opa.rego.ast.Expression;
import eu.contentcloud.abac.opa.rego.ast.Term;
import eu.contentcloud.abac.opa.rego.ast.Term.Numeric;
import eu.contentcloud.abac.opa.rego.ast.Term.Text;
import eu.contentcloud.abac.predicates.model.FunctionExpression.Operator;
import eu.contentcloud.abac.predicates.model.SymbolicReference;
import java.util.List;
import org.junit.jupiter.api.Test;

class QuerySetToPbacExpressionConverterTest {

    @Test
    void partialEval_clearanceLevel_gte_4() {
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

        var expr = new QuerySetToPbacExpressionConverter().convert(opaExpr);
        assertThat(expr).isNotNull().isInstanceOf(Comparison.class);
        assertThat((Comparison) expr).satisfies(comp -> {
            assertThat(comp.getOperator()).isEqualTo(Operator.GREATER_THAN_OR_EQUAL_TO);

            assertThat(comp.getLeftTerm().canBeResolved()).isTrue();
            assertThat(comp.getLeftTerm().resolve()).isEqualTo(4);

            assertThat(comp.getRightTerm().canBeResolved()).isFalse();
            assertThat(comp.getRightTerm()).isInstanceOf(SymbolicReference.class);

            assertThat(comp.canBeResolved()).isFalse();
        });
    }

    @Test
    void partialEval_attributeGroup_matches() {
        //     infix:   "black mesa" == data.documents[_].group
        // expr call:   eq ( "black mesa", data.documents[_].group )
        var opaExpr = new Expression(0, List.of(
                new Term.Ref(List.of(new Term.Var("eq"))),
                new Text("Black Mesa"),
                new Term.Ref(List.of(
                        new Term.Var("data"),
                        new Text("documents"),
                        new Term.Var("$01"),
                        new Text("group")
                ))
        ));

        var expr = new QuerySetToPbacExpressionConverter().convert(opaExpr);
        assertThat(expr).isNotNull().isInstanceOf(Comparison.class);
        assertThat((Comparison) expr).satisfies(comp -> {
            assertThat(comp.getOperator()).isEqualTo(Operator.EQUALS);

            assertThat(comp.getLeftTerm().canBeResolved()).isTrue();
            assertThat(comp.getLeftTerm().resolve()).isEqualTo("Black Mesa");

            assertThat(comp.getRightTerm().canBeResolved()).isFalse();
            assertThat(comp.getRightTerm()).isInstanceOf(SymbolicReference.class);

            assertThat(comp.canBeResolved()).isFalse();
        });
    }

}