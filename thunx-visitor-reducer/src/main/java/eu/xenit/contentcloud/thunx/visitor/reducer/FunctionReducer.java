package eu.xenit.contentcloud.thunx.visitor.reducer;

import eu.xenit.contentcloud.thunx.predicates.model.ThunkExpression;
import java.util.List;
import java.util.Optional;

@FunctionalInterface
public interface FunctionReducer<T> {
    FunctionReducer<?> NO_OP = (values) -> Optional.empty();

    Optional<ThunkExpression<T>> tryReduce(List<ThunkExpression<?>> values);
}
