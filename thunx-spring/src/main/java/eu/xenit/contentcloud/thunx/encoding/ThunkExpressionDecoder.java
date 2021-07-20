package eu.xenit.contentcloud.thunx.encoding;

import eu.xenit.contentcloud.thunx.predicates.model.Expression;

@FunctionalInterface
public
interface ThunkExpressionDecoder {

    Expression<Boolean> decoder(byte[] data);
}
