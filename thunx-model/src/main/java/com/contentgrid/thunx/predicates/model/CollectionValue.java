package com.contentgrid.thunx.predicates.model;

import lombok.EqualsAndHashCode;

import java.util.Collection;
import java.util.Optional;

//@EqualsAndHashCode(exclude = "type")
public interface CollectionValue<T extends Collection> extends ThunkExpression<Collection<ThunkExpression<?>>> {

    T getValue();

//    private final Collection<Scalar<?>> value;
//
//    private final Class<? extends Collection<Scalar<?>>> type;
//
//    public CollectionValue(Collection<Scalar<?>> value, Class<? extends Collection<Scalar<?>>> type) {
//        this.value = value;
//        this.type = type;
//    }
//
//    public CollectionValue(Collection<Scalar<?>> value) {
//        this(value, (Class<? extends Collection<Scalar<?>>>)value.getClass());
//    }
//
//    @Override
//    public Collection<Scalar<?>> getValue() {
//        return value;
//    }

//    @Override
//    Class<? extends Collection<ThunkExpression<?>>> getResultType() {
//        return type;
//    }
//
//    @Override
//    public <R, C> R accept(ThunkExpressionVisitor<R, C> visitor, C context) {
//        return visitor.visit(this, context);
//    }

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

}
