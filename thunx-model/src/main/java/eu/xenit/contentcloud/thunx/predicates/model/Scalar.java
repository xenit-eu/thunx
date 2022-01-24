package eu.xenit.contentcloud.thunx.predicates.model;

import java.math.BigDecimal;
import java.util.Optional;

public interface Scalar<T> extends ThunkExpression<T> {

    static <R> Optional<Scalar<R>> maybeScalar(ThunkExpression<R> expression) {
        if(expression instanceof Scalar) {
            return Optional.of((Scalar<R>) expression);
        }
        return Optional.empty();
    }

    static <R> Optional<R> maybeValue(ThunkExpression<R> expression) {
        if(expression.getResultType() == Void.class) {
            throw new IllegalArgumentException("Expression with result type "+Void.class+" can not be mapped with #maybeValue(), use #maybeScalar() instead.");
        }
        return maybeScalar(expression)
                .map(Scalar::getValue);
    }

    T getValue();

    @Override
    default ThunkExpression<T> simplify() {
        return this;
    }

    default <R> R accept(ThunkExpressionVisitor<R> visitor) {
        return visitor.visit(this);
    }

    static NumberValue of(BigDecimal number) {
        return new NumberValue(number);
    }

    static NumberValue of(double number) {
        return of(BigDecimal.valueOf(number));
    }

    static NumberValue of(long number) {
        return of(BigDecimal.valueOf(number));
    }

    static StringValue of(String value) {
        return new StringValue(value);
    }

    static BooleanValue of(boolean value) {
        return new BooleanValue(value);
    }

    static NullValue nullValue() {
        return NullValue.INSTANCE;
    }
}
