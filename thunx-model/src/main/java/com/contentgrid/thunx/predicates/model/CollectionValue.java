package com.contentgrid.thunx.predicates.model;

import java.util.Collection;

public interface CollectionValue<T extends Collection<?>> extends ThunkExpression<Collection<ThunkExpression<?>>> {

    T getValue();

}
