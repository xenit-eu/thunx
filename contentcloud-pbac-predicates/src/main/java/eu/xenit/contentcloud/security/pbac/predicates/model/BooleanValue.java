package eu.xenit.contentcloud.security.pbac.predicates.model;

class BooleanValue extends Scalar<Boolean> {

    protected BooleanValue(Boolean value) {
        super(value);
    }

    @Override
    public Class<? extends Boolean> getResultType() {
        return Boolean.class;
    }
}
