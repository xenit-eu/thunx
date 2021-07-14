package eu.contentcloud.abac.encoding;

import eu.contentcloud.abac.predicates.model.Expression;

@FunctionalInterface
public
interface AbacExpressionDecoder {

    Expression<Boolean> decoder(byte[] data);
}
