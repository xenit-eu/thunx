package com.contentgrid.thunx.encoding;

import com.contentgrid.thunx.predicates.model.ThunkExpression;

@FunctionalInterface
public
interface ThunkExpressionEncoder {

    byte[] encode(ThunkExpression<Boolean> expression);
}
