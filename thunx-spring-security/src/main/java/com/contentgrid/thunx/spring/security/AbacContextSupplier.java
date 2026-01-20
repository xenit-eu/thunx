package com.contentgrid.thunx.spring.security;

import com.contentgrid.thunx.predicates.model.ThunkExpression;

public interface AbacContextSupplier {
    ThunkExpression<Boolean> getAbacContext();
}
