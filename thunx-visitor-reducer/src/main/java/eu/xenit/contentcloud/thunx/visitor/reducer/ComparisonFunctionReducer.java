package eu.xenit.contentcloud.thunx.visitor.reducer;

import eu.xenit.contentcloud.thunx.predicates.model.Scalar;
import eu.xenit.contentcloud.thunx.predicates.model.ThunkExpression;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;

@AllArgsConstructor
class ComparisonFunctionReducer implements FunctionReducer<Boolean> {

    @FunctionalInterface
    public interface ComparisonFunction {
        boolean eval(Object value1, Object value2);
    }

    private final ComparisonFunction comparisonFunction;

    @Override
    public Optional<ThunkExpression<Boolean>> tryReduce(List<ThunkExpression<?>> values) {
        if(values.size() != 2) {
            throw new IllegalArgumentException("Comparison functions require exactly 2 parameters, received "+values.size()+" parameters.");
        }
        var availableValues = values.stream()
                .map(ThunkExpression::maybeScalar)
                .collect(Collectors.toList());
        if(availableValues.stream().allMatch(Optional::isPresent)) {
            var unwrappedValues = availableValues.stream()
                    .map(Optional::get)
                    .map(Scalar::getValue)
                    .collect(Collectors.toList());
            return Optional.of(Scalar.of(comparisonFunction.eval(unwrappedValues.get(0), unwrappedValues.get(1))));
        }
        return Optional.empty();
    }
}
