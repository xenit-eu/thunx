package com.contentgrid.thunx.visitor.reducer;

import com.contentgrid.thunx.predicates.model.CollectionValue;
import com.contentgrid.thunx.predicates.model.FunctionExpression;
import com.contentgrid.thunx.predicates.model.FunctionExpression.Operator;
import com.contentgrid.thunx.predicates.model.ListValue;
import com.contentgrid.thunx.predicates.model.LogicalOperation;
import com.contentgrid.thunx.predicates.model.Scalar;
import com.contentgrid.thunx.predicates.model.SetValue;
import com.contentgrid.thunx.predicates.model.SymbolicReference;
import com.contentgrid.thunx.predicates.model.ThunkExpression;
import com.contentgrid.thunx.predicates.model.ContextFreeThunkExpressionVisitor;
import com.contentgrid.thunx.predicates.model.Variable;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Singular;

@AllArgsConstructor
@Builder
public class ThunkReducerVisitor extends ContextFreeThunkExpressionVisitor<ThunkExpression<?>> {

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
                        .map(expression -> expression.accept(this, null))
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

    @Override
    public ThunkExpression<?> visit(SetValue setValue) {
        return setValue;
    }

    @Override
    public ThunkExpression<?> visit(ListValue listValue) {
        return listValue;
    }
}
