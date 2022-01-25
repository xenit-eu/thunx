package eu.xenit.contentcloud.thunx.predicates.model;

/**
 * predicates are expressions (or sets of expressions) combined with logical operators, and are evaluated as booleans
 */
public interface BooleanOperation extends FunctionExpression<Boolean> {

    @Override
    default Class<? extends Boolean> getResultType() {
        return Boolean.class;
    }
}
