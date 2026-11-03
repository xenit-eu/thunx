package com.contentgrid.thunx.encoding;

import com.contentgrid.thunx.predicates.model.ThunkExpression;

@FunctionalInterface
public interface ThunkExpressionDecoder {

    ThunkExpression<Boolean> decode(byte[] data);
}
