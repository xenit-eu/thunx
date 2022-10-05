package com.contentgrid.thunx.visitor.reducer;

import com.contentgrid.thunx.predicates.model.ThunkExpression;
import java.util.List;
import java.util.Optional;

@FunctionalInterface
public interface FunctionReducer<T> {
    FunctionReducer<?> NO_OP = (values) -> Optional.empty();

    Optional<ThunkExpression<T>> tryReduce(List<ThunkExpression<?>> values);
}
