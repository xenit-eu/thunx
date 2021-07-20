package eu.xenit.contentcloud.thunx.predicates.converters.json;

import java.lang.reflect.Type;
import lombok.Getter;

public class InvalidExpressionDataException extends RuntimeException {


    public InvalidExpressionDataException(String message) {
        super(message);
    }

    static class InvalidExpressionTypeException extends InvalidExpressionDataException {
        @Getter
        private String type;

        public InvalidExpressionTypeException(String type, String message) {
            super(message);
            this.type = type;
        }
    }

    @Getter
    static class InvalidExpressionValueException extends InvalidExpressionDataException {

        private Object value;
        private Type expected;

        public InvalidExpressionValueException(Object value, Type expected) {
            super(String.format("Expected value '%s' to be of type '%s', but was '%s'", value, expected, type(value)));

            this.value = value;
            this.expected = expected;
        }

        private static Type type(Object value) {
            if (value == null) {
                return Void.class;
            }
            return value.getClass();
        }
    }
}
