package com.contentgrid.thunx.visitor.reducer;

import com.contentgrid.thunx.predicates.model.FunctionExpression;
import com.contentgrid.thunx.predicates.model.FunctionExpression.Operator;
import com.contentgrid.thunx.predicates.model.LogicalOperation;
import com.contentgrid.thunx.predicates.model.Scalar;
import com.contentgrid.thunx.predicates.model.SymbolicReference;
import com.contentgrid.thunx.predicates.model.ThunkExpression;
import com.contentgrid.thunx.predicates.model.ThunkExpressionVisitor;
import com.contentgrid.thunx.predicates.model.Variable;
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
            .operatorReducer(Operator.AND, new LogicalFunctionReducer(false, true, LogicalOperation::uncheckedConjunction))
            .operatorReducer(Operator.OR, new LogicalFunctionReducer(true, false,  LogicalOperation::uncheckedDisjunction))
            .build();

    @Singular
    private final Map<Operator, FunctionReducer<?>> operatorReducers;

    @Override
    public ThunkExpression<?> visit(Scalar<?> scalar) {
        return scalar;
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
