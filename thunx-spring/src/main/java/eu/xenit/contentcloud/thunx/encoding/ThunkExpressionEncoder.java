package eu.xenit.contentcloud.thunx.encoding;

import eu.xenit.contentcloud.thunx.predicates.model.Expression;

@FunctionalInterface
public
interface ThunkExpressionEncoder {

    byte[] encode(Expression<Boolean> expression);
}
