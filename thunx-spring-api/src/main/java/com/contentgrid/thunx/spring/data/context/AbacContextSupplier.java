package com.contentgrid.thunx.spring.data.context;

import com.contentgrid.thunx.predicates.model.ThunkExpression;

/**
 * @deprecated Use {@link com.contentgrid.thunx.spring.security.AbacContextSupplier} instead
 */
@Deprecated(since = "0.14.1")
public interface AbacContextSupplier {
    ThunkExpression<Boolean> getAbacContext();
}
