package eu.xenit.contentcloud.thunx.visitor.reducer;

import eu.xenit.contentcloud.thunx.predicates.model.FunctionExpression;
import eu.xenit.contentcloud.thunx.predicates.model.FunctionExpression.FunctionExpressionFactory;
import eu.xenit.contentcloud.thunx.predicates.model.FunctionExpression.Operator;
import eu.xenit.contentcloud.thunx.predicates.model.Scalar;
import eu.xenit.contentcloud.thunx.predicates.model.SymbolicReference;
import eu.xenit.contentcloud.thunx.predicates.model.ThunkExpression;
import eu.xenit.contentcloud.thunx.predicates.model.ThunkExpressionVisitor;
import eu.xenit.contentcloud.thunx.predicates.model.Variable;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Singular;

@AllArgsConstructor
@Builder
public class ThunkReducerVisitor implements ThunkExpressionVisitor<ThunkExpression<?>> {

    public static ThunkReducerVisitor DEFAULT_INSTANCE = ThunkReducerVisitor.builder()
            .operatorReducer(Operator.EQUALS, new ComparisonFunctionReducer(Objects::equals))
            .operatorReducer(Operator.AND, new LogicalFunctionReducer(false, true,
                    (FunctionExpressionFactory<Boolean>) Operator.AND.getFactory()))
            .operatorReducer(Operator.OR, new LogicalFunctionReducer(true, false,
                    (FunctionExpressionFactory<Boolean>) Operator.OR.getFactory()))
            .build();

    @Singular
    private final Map<Operator, FunctionReducer<?>> operatorReducers;

    @Override
    public ThunkExpression<?> visit(Scalar<?> scalar) {
        return (ThunkExpression<Object>) scalar;
    }

    @Override
    public ThunkExpression<?> visit(FunctionExpression<?> functionExpression) {
        var reducer = operatorReducers.getOrDefault(functionExpression.getOperator(), FunctionReducer.NO_OP);

        return reducer.tryReduce(
                functionExpression.getTerms()
                        .stream()
                        .map(expression -> expression.accept(this))
                        .collect(Collectors.toList())
        ).orElse((ThunkExpression)functionExpression);
    }

    @Override
    public ThunkExpression<?> visit(SymbolicReference symbolicReference) {
        return symbolicReference;
    }

    @Override
    public ThunkExpression<?> visit(Variable variable) {
        return variable;
    }
}
