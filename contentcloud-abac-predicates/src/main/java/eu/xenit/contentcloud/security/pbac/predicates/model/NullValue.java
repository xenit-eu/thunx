package eu.xenit.contentcloud.security.pbac.predicates.model;

class NullValue extends Scalar<Void> {

    protected NullValue() {
        super(null);
    }

    @Override
    public Class<? extends Void> getResultType() {
        return Void.class;
    }
}
