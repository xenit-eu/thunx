package com.contentgrid.thunx.spring.data.context;

import com.contentgrid.thunx.predicates.model.ThunkExpression;

public interface AbacContextSupplier {
    ThunkExpression<Boolean> getAbacContext();
}
