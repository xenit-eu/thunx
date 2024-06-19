package com.contentgrid.thunx.encoding;

import com.contentgrid.thunx.predicates.model.ThunkExpression;

@FunctionalInterface
public
interface ThunkExpressionDecoder {

    /**
     * @deprecated use {@link #decode(byte[])} instead
     */
    @Deprecated(forRemoval = true, since = "0.10.1")
    ThunkExpression<Boolean> decoder(byte[] data);

    @SuppressWarnings("deprecation")
    default ThunkExpression<Boolean> decode(byte[] data) {
        return this.decoder(data);
    }
}
