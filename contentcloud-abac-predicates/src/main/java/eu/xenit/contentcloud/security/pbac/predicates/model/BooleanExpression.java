package eu.xenit.contentcloud.security.pbac.predicates.model;

/**
 * predicates are expressions (or sets of expressions) combined with logical operators, and are evaluated as booleans
 */
public interface BooleanExpression extends FunctionExpression<Boolean> {

    @Override
    default Class<? extends Boolean> getResultType() {
        return Boolean.class;
    }

    default Boolean resolve() {
       throw new UnsupportedOperationException("resolve not implemented");
    }
}
