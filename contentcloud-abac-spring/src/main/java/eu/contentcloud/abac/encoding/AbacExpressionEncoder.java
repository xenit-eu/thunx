package eu.contentcloud.abac.encoding;

import eu.contentcloud.abac.predicates.model.Expression;

@FunctionalInterface
public
interface AbacExpressionEncoder {

    byte[] encode(Expression<Boolean> expression);
}
