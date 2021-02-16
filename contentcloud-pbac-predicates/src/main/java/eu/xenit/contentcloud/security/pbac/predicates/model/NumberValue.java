package eu.xenit.contentcloud.security.pbac.predicates.model;

class NumberValue extends Scalar<Number> {

    NumberValue(Number value) {
        super(value);
    }

    @Override
    public Class<? extends Number> getResultType() {
        return Number.class;
    }
}
