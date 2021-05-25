package eu.xenit.contentcloud.security.pbac.predicates.model;

class StringValue extends Scalar<String> {

    protected StringValue(String value) {
        super(value);
    }

    @Override
    public Class<? extends String> getResultType() {
        return String.class;
    }
}
