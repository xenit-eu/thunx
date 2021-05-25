package eu.xenit.contentcloud.security.pbac.predicates.model;

public abstract class Scalar<T> implements Expression<T> {


    private final T value;

    protected Scalar(T value) {
        this.value = value;
    }

    public T getValue() {
        return this.value;
    }

    @Override
    public boolean canBeResolved() {
        return true;
    }

    @Override
    public T resolve() {
        return value;
    }

    public <R> R accept(ExpressionVisitor<R> visitor) {
        return visitor.visit(this);
    }

    public static NumberValue of(Number number) {
        return new NumberValue(number);
    }

    public static NumberValue of(double number) {
        return new NumberValue(number);
    }

    public static NumberValue of(long number) {
        return new NumberValue(number);
    }

    public static StringValue of(String value) {
        return new StringValue(value);
    }

    public static BooleanValue of(boolean value) {
        return new BooleanValue(value);
    }

    public static NullValue nullValue() {
        return new NullValue();
    }
}
