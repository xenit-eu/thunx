package eu.xenit.contentcloud.thunx.encoding;

import eu.xenit.contentcloud.thunx.predicates.model.ThunkExpression;

@FunctionalInterface
public
interface ThunkExpressionEncoder {

    byte[] encode(ThunkExpression<Boolean> expression);
}
