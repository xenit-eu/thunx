package com.contentgrid.thunx.pdp.opa;


import com.contentgrid.opa.rego.ast.Query;
import com.contentgrid.opa.rego.ast.QuerySet;
import com.contentgrid.opa.rego.ast.RegoVisitor;
import com.contentgrid.opa.rego.ast.Term;
import com.contentgrid.opa.rego.ast.Term.ArrayTerm;
import com.contentgrid.opa.rego.ast.Term.Bool;
import com.contentgrid.opa.rego.ast.Term.Call;
import com.contentgrid.opa.rego.ast.Term.Null;
import com.contentgrid.opa.rego.ast.Term.Numeric;
import com.contentgrid.opa.rego.ast.Term.Ref;
import com.contentgrid.opa.rego.ast.Term.SetTerm;
import com.contentgrid.opa.rego.ast.Term.Text;
import com.contentgrid.opa.rego.ast.Term.Var;
import com.contentgrid.thunx.predicates.model.Comparison;
import com.contentgrid.thunx.predicates.model.LogicalOperation;
import com.contentgrid.thunx.predicates.model.NumericFunction;
import com.contentgrid.thunx.predicates.model.Scalar;
import com.contentgrid.thunx.predicates.model.SymbolicReference;
import com.contentgrid.thunx.predicates.model.SymbolicReference.StringPathElement;
import com.contentgrid.thunx.predicates.model.ThunkExpression;
import com.contentgrid.thunx.predicates.model.Variable;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class QuerySetToThunkExpressionConverter {

    public ThunkExpression<Boolean> convert(QuerySet querySet) {
        // if the query-set is empty, that means that under NO conditions, the expression can be true
        if (querySet == null || querySet.size() == 0) {
            return Scalar.of(false);
        }

        // query-set is a disjunction (OR-list of terms)
        var queries = querySet.stream().map(this::convert);

        // optimization: if the query-set contains only 1 query, no need to wrap that in a disjunction
        if (querySet.size() == 1) {
            return queries.findFirst().orElseThrow();
        }

        return LogicalOperation.disjunction(queries);
    }


    public ThunkExpression<Boolean> convert(Query query) {
        // a query is a conjunction (AND-list of terms)

        // optimization: if query has 0 terms, it means the condition is satisfied
        // OPA uses this when there is an unconditional ALLOW as a result on a partial-eval-query
        if (query.size() == 0) {
            return Scalar.of(true);
        }

        var expressions = query.stream()
                .map(this::convert)
                .peek(expr -> {
                    if (!Boolean.class.isAssignableFrom(expr.getResultType())) {
                        // there are non-boolean expressions in here ?
                        var msg = String.format("Expected boolean expression, but was of type %s",
                                expr.getResultType().getSimpleName());
                        throw new UnsupportedOperationException(msg);
                    }
                })
                .map(expr -> (ThunkExpression<Boolean>) expr)
                .collect(Collectors.toList());

        // Optimize: if there is a single expression, unwrap the conjunction
        if (expressions.size() == 1) {
            return expressions.get(0);
        }

        return LogicalOperation.conjunction(expressions);
    }

    ThunkExpression<?> convert(com.contentgrid.opa.rego.ast.Expression expression) {
        var expr = expression.accept(new PredicatesVisitor());
        return expr;
    }

    static class PredicatesVisitor implements RegoVisitor<ThunkExpression<?>> {

        private static Map<String, Function<List<ThunkExpression<?>>, ThunkExpression<?>>> OPERATION_LOOKUP = Map.ofEntries(
                Map.entry("mul", NumericFunction::multiply),

                Map.entry("eq", Comparison::areEqual),
                Map.entry("neq", Comparison::notEqual),

                Map.entry("gt", Comparison::greater),
                Map.entry("gte", Comparison::greaterOrEquals),
                Map.entry("lt", Comparison::less),
                Map.entry("lte", Comparison::lessOrEquals)
        );

        @Override
        public ThunkExpression<?> visit(QuerySet queries) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ThunkExpression<?> visit(Query query) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ThunkExpression<?> visit(com.contentgrid.opa.rego.ast.Expression expression) {

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
            ThunkExpression<?> firstExpr = firstTerm.accept(this);

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
                List<ThunkExpression<?>> args = expression.getTerms().stream().skip(1)
                        .map(t -> t.accept(this))
                        .collect(Collectors.toList());

                var operation = OPERATION_LOOKUP.get(functionName);
                if (operation != null) {
                    return operation.apply(args);
                } else {
                    throw new UnsupportedOperationException("operation " + functionName + " not supported");
                }

            } else {
                var msg = "cannot translate fields in top level expression (index:" + expression.getIndex()
                        + ") into a proper predicate:"
                        + expression.getTerms().stream().map(term -> term.getClass().getName())
                        .collect(Collectors.joining(","));
                throw new UnsupportedOperationException(msg);

            }
        }

        @Override
        public ThunkExpression<?> visit(Ref ref) {
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
                var symbol = SymbolicReference.of((Variable) subject, ref.getValue().stream().skip(1).map(val -> {
                    if (val instanceof Term.Text) {
                        return SymbolicReference.path(((Term.Text) val).getValue());
                    } else if (val instanceof Var) {
                        return SymbolicReference.pathVar(((Var) val).getValue());
                    }
                    String msg = String.format("ref-arg of type %s not implemented", val.getClass().getName());
                    throw new RuntimeException(msg);
                }));

                // the 'entity' unknown variable is expressed in OPA policies as 'input.entity'
                // in the thunk-expression we lift the entity to the top-level
                // 'input' is after all an OPA implementation detail
                var path = symbol.getPath();
                if ("input".equalsIgnoreCase(symbol.getSubject().getName())) {
                    symbol = symbol.getPath().stream().findFirst()
                            .filter(first -> first instanceof StringPathElement)
                            .filter(first -> "entity".equalsIgnoreCase(first.toString()))
                            .map(first -> SymbolicReference.of(Variable.named("entity"), path.stream().skip(1)))
                            .orElse(symbol);
                }

                return symbol;

            } else {
                // non-variable symbolic-references not yet implemented
                String message = String.format("symbolic ref for <%s> not implemented", subject.getClass().getName());
                throw new UnsupportedOperationException(message);
            }
        }

        @Override
        public ThunkExpression<?> visit(Call call) {
            // 1st term is the operator
            // 2nd..Nth term are the arguments
            throw new UnsupportedOperationException();
        }

        @Override
        public ThunkExpression<?> visit(Var var) {
            return Variable.named(var.getValue());
        }

        @Override
        public ThunkExpression<?> visit(Numeric numeric) {
            // numeric uses BigDecimal internally
            return Scalar.of(numeric.getValue());
        }

        @Override
        public ThunkExpression<?> visit(Text stringValue) {
            return Scalar.of(stringValue.getValue());
        }

        @Override
        public ThunkExpression<?> visit(Bool value) {
            return Scalar.of(value.getValue());
        }

        @Override
        public ThunkExpression<?> visit(Null nullValue) {
            return Scalar.nullValue();
        }

        @Override
        public ThunkExpression<?> visit(ArrayTerm arrayTerm) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ThunkExpression<?> visit(SetTerm setTerm) {
            throw new UnsupportedOperationException();
        }
    }

}
