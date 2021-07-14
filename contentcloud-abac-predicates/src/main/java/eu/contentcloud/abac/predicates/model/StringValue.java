package eu.contentcloud.abac.predicates.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode
class StringValue implements Scalar<String> {

    @Getter
    private String value;

    protected StringValue(String value) {

        this.value = value;
    }

    @Override
    public String toString() {
        return "'" + this.getValue() + "'";
    }

    @Override
    public Class<? extends String> getResultType() {
        return String.class;
    }
}
