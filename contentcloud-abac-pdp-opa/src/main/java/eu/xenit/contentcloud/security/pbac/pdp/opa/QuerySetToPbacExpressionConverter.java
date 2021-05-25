package eu.xenit.contentcloud.security.pbac.pdp.opa;

import eu.xenit.contentcloud.abac.opa.rego.ast.Query;
import eu.xenit.contentcloud.abac.opa.rego.ast.QuerySet;
import eu.xenit.contentcloud.abac.opa.rego.ast.Term;
import eu.xenit.contentcloud.abac.opa.rego.ast.Term.ArrayTerm;
import eu.xenit.contentcloud.abac.opa.rego.ast.Term.Bool;
import eu.xenit.contentcloud.abac.opa.rego.ast.Term.Call;
import eu.xenit.contentcloud.abac.opa.rego.ast.Term.Null;
import eu.xenit.contentcloud.abac.opa.rego.ast.Term.Numeric;
import eu.xenit.contentcloud.abac.opa.rego.ast.Term.Ref;
import eu.xenit.contentcloud.abac.opa.rego.ast.Term.Text;
import eu.xenit.contentcloud.abac.opa.rego.ast.Term.Var;
import eu.xenit.contentcloud.abac.opa.rego.ast.RegoVisitor;
import eu.xenit.contentcloud.security.pbac.predicates.model.NumericFunction;
import eu.xenit.contentcloud.security.pbac.predicates.model.Comparison;
import eu.xenit.contentcloud.security.pbac.predicates.model.Conjunction;
import eu.xenit.contentcloud.security.pbac.predicates.model.Disjunction;
import eu.xenit.contentcloud.security.pbac.predicates.model.Expression;
import eu.xenit.contentcloud.security.pbac.predicates.model.Scalar;
import eu.xenit.contentcloud.security.pbac.predicates.model.SymbolicReference;
import eu.xenit.contentcloud.security.pbac.predicates.model.Variable;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class QuerySetToPbacExpressionConverter {

    public Expression<Boolean> convert(QuerySet querySet) {
        if (querySet == null || querySet.size() == 0) {
            throw new IllegalArgumentException("did not expect empty query set");
        }

        // query-set is a disjunction (OR a list of terms)
        return Disjunction.of(querySet.stream().map(this::convert));
    }


    public Expression<Boolean> convert(Query query) {
        // a query is a conjunction (AND a list of terms)
        // this _should_ be a boolean expression, or this would not make sense ?

        var expressions = query.stream()
                .map(this::convert)
                .peek(expr -> {
                    if (!Boolean.class.isAssignableFrom(expr.getResultType())) {
                        // there are non-boolean expressions in here ?
                        String msg = "expression is expected to be a boolean expression, but was " + expr.getResultType();
                        throw new UnsupportedOperationException(msg);
                    }
                })
                .map(expr -> (Expression<Boolean>) expr);

        return Conjunction.of(expressions);
    }

    Expression<?> convert(eu.xenit.contentcloud.abac.opa.rego.ast.Expression expression) {
        var expr = expression.accept(new PredicatesVisitor());
        return expr;
    }

    static class PredicatesVisitor implements RegoVisitor<Expression<?>> {

        private static Map<String, Function<List<Expression<?>>, Expression<?>>> OPERATION_LOOKUP = Map.ofEntries(
                Map.entry("mul", NumericFunction::multiply),

                Map.entry("eq", Comparison::areEqual),
                Map.entry("gte", Comparison::greaterOrEquals)
        );

        @Override
        public Expression<?> visit(QuerySet queries) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Expression<?> visit(Query query) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Expression<?> visit(eu.xenit.contentcloud.abac.opa.rego.ast.Expression expression) {

            // opa-expression grammar - bottom page https://www.openpolicyagent.org/docs/latest/policy-reference/
            //      expr            = term | expr-call | expr-infix
            //      term            = ref | var | scalar | array | object | set | array-compr | object-compr | set-compr
            //      expr-call       = var [ "." var ] "(" [ term { "," term } ] ")"
            //      expr-infix      = [ term "=" ] term infix-operator term
            //      infix-operator  = bool-operator | arith-operator | bin-operator
            //      ref             = ( var | array | object | set | array-compr | object-compr | set-compr | expr-call ) { ref-arg }
            //      ref-arg         = ref-arg-dot | ref-arg-brack
            //      ref-arg-brack   = "[" ( scalar | var | array | object | set | "_" ) "]"
            //      ref-arg-dot     = "." var
            //      var             = ( ALPHA | "_" ) { ALPHA | DIGIT | "_" }
            //
            // Remarks:
            // 1. after query evaluation, the 'expr-infix' seems to be re-written as expr-calls
            // for example the expr-infix: '3.14 * input.radius'
            // is evaluated into: call( (ref(var(mul)), number(3.14), ref(var(input),string(radius)) )
            //
            // 2. we're parsing an 'expr' here, which potentially could be just a 'term'
            // for example: `allow { input.isAdmin }`
            //
            // 3. the top-level expression is not wrapped in a call-term, but it does looks like the contents of a call-term
            //
            // 4. according to the grammar, an 'expr-call' should start with a 'var' - but the AST always seem to
            // wrap that in a 'ref' - can those be simply unwrapped ?
            //
            // 5. either the expression is:
            // a) 1 term (ref/var/scalar/...)
            // b) an 'expr-call' with 1+n terms (n=0..N), first term is a (ref-wrapped) var, followed by call arguments
            //
            // 6. The 'term' argument for an expr-call can be another expr-call:
            // 'term' contains 'ref, ref contains expr-call.


            if (expression.getTerms().isEmpty()) {
                throw new IllegalArgumentException("expression is empty");
            }

            var firstTerm = expression.getTerms().get(0);
            Expression<?> firstExpr = firstTerm.accept(this);

            // if there is a single term, just return already
            if (expression.getTerms().size() == 1) {
                return firstExpr;
            }

            // if there are multiple terms, it is (should be?) a call-expression
            // call expressions start with a symbolic reference
            if (firstExpr instanceof SymbolicReference) {
                // the first symbolic-ref would be a function name
                var functionName = ((SymbolicReference) firstExpr).toPath();
                // lookup function names
                List<Expression<?>> args = expression.getTerms().stream().skip(1)
                        .map(t -> t.accept(this))
                        .collect(Collectors.toList());

                var operation = OPERATION_LOOKUP.get(functionName);
                if (operation != null) {
                    return operation.apply(args);
                }
                else {
                    throw new UnsupportedOperationException("operation "+functionName+" not supported");
                }

            } else {
                var msg = "cannot translate fields in top level expression (index:"+expression.getIndex()+") into a proper predicate:"
                        + expression.getTerms().stream().map(term -> term.getClass().getName())
                            .collect(Collectors.joining(","));
                throw new UnsupportedOperationException(msg);

            }





        }

        @Override
        public Expression<?> visit(Ref ref) {
            if (ref.getValue().isEmpty()) {
                throw new IllegalArgumentException("ref has no values");
            }

            // Grammar:
            //      ref             = ( var | array | object | set | array-compr | object-compr | set-compr | expr-call ) { ref-arg }
            //      ref-arg-brack   = "[" ( scalar | var | array | object | set | "_" ) "]"
            // Parsed Ref example:
            // {
            //              "type": "ref",
            //              "value": [
            //                {
            //                  "type": "var",
            //                  "value": "data"
            //                },
            //                {
            //                  "type": "string",
            //                  "value": "reports"
            //                },
            //                {
            //                  "type": "var",
            //                  "value": "$01"
            //                },
            //                {
            //                  "type": "string",
            //                  "value": "clearance_level"
            //                }
            //              ]
            //            }

            var subject = ref.getValue().get(0).accept(this);
            if (subject instanceof Variable) {
                return SymbolicReference.of((Variable) subject, ref.getValue().stream().skip(1).map(val -> {
                    if (val instanceof Term.Text) {
                        return SymbolicReference.path(((Text) val).getValue());
                    } else if (val instanceof Var) {
                        return SymbolicReference.var(((Var) val).getValue());
                    }
                    throw new RuntimeException(String.format("ref-arg of type %s not implemented", val.getClass().getName()));
                }));

            } else {
                // non-variable symbolic-references not yet implemented
                String message = String.format("symbolic ref for <%s> not implemented", subject.getClass().getName());
                throw new UnsupportedOperationException(message);
            }
        }

        @Override
        public Expression<?> visit(Call call) {
            // 1st term is the operator
            // 2nd..Nth term are the arguments
            return null;
        }

        @Override
        public Expression<?> visit(Var var) {
            return Variable.named(var.getValue());
        }

        @Override
        public Expression<?> visit(Numeric numberValue) {
            return Scalar.of(numberValue.getValue());
        }

        @Override
        public Expression<?> visit(Text stringValue) {
            return Scalar.of(stringValue.getValue());
        }

        @Override
        public Expression<?> visit(Bool value) {
            return Scalar.of(value.getValue());
        }

        @Override
        public Expression<?> visit(Null nullValue) {
            return Scalar.nullValue();
        }

        @Override
        public Expression<?> visit(ArrayTerm arrayTerm) {
            return null;
        }
    }

}
