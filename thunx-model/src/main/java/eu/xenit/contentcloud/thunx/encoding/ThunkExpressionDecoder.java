package eu.xenit.contentcloud.thunx.encoding;

import eu.xenit.contentcloud.thunx.predicates.model.ThunkExpression;

@FunctionalInterface
public
interface ThunkExpressionDecoder {

    ThunkExpression<Boolean> decoder(byte[] data);
}
